package br.com.ide.data.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import br.com.ide.MainActivity
import br.com.ide.R
import br.com.ide.domain.location.MissionLocationTracker
import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.usecase.GetUserByIdUseCase
import br.com.ide.domain.usecase.ObserveMissionParticipantsUseCase
import br.com.ide.domain.usecase.RecordMissionTrackPointUseCase
import br.com.ide.domain.usecase.SyncPendingMissionTrackPointsUseCase
import br.com.ide.domain.usecase.UpdateMissionParticipantLocationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class MissionLocationService : Service() {

    @Inject
    lateinit var missionLocationTracker: MissionLocationTracker

    @Inject
    lateinit var updateMissionParticipantLocationUseCase:
            UpdateMissionParticipantLocationUseCase

    @Inject
    lateinit var recordMissionTrackPointUseCase:
            RecordMissionTrackPointUseCase

    @Inject
    lateinit var syncPendingMissionTrackPointsUseCase:
            SyncPendingMissionTrackPointsUseCase

    @Inject
    lateinit var observeMissionParticipantsUseCase:
            ObserveMissionParticipantsUseCase

    @Inject
    lateinit var getUserByIdUseCase:
            GetUserByIdUseCase

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private var trackingJob: Job? = null
    private var syncJob: Job? = null
    private var supportObservationJob: Job? = null

    private var activeMissionId: String? = null
    private var activeUserId: String? = null

    private val notifiedSupportRequestUserIds =
        mutableSetOf<String>()

    override fun onCreate() {
        super.onCreate()

        createLocationNotificationChannel()
        createSupportNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action == ACTION_STOP) {
            stopTracking()
            stopSelf()
            return START_NOT_STICKY
        }

        val missionId =
            intent
                ?.getStringExtra(EXTRA_MISSION_ID)
                ?.takeIf { it.isNotBlank() }
                ?: return START_NOT_STICKY

        val userId =
            intent
                .getStringExtra(EXTRA_USER_ID)
                ?.takeIf { it.isNotBlank() }
                ?: return START_NOT_STICKY

        promoteToForeground()

        if (
            activeMissionId == missionId &&
            activeUserId == userId &&
            trackingJob?.isActive == true &&
            supportObservationJob?.isActive == true
        ) {
            return START_NOT_STICKY
        }

        activeMissionId = missionId
        activeUserId = userId

        startTracking(
            missionId = missionId,
            userId = userId
        )

        startSupportObservation(
            missionId = missionId,
            userId = userId
        )

        startPendingSyncLoop()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun promoteToForeground() {

        val notification =
            NotificationCompat
                .Builder(
                    this,
                    LOCATION_NOTIFICATION_CHANNEL_ID
                )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(
                    getString(
                        R.string.mission_location_notification_title
                    )
                )
                .setContentText(
                    getString(
                        R.string.mission_location_notification_text
                    )
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()

        val foregroundServiceType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }

        ServiceCompat.startForeground(
            this,
            LOCATION_NOTIFICATION_ID,
            notification,
            foregroundServiceType
        )
    }

    private fun startTracking(
        missionId: String,
        userId: String
    ) {

        trackingJob?.cancel()

        trackingJob =
            serviceScope.launch {
                missionLocationTracker
                    .observeLocation()
                    .catch {
                    }
                    .collect { location ->

                        launch {
                            runCatching {
                                updateMissionParticipantLocationUseCase(
                                    missionId = missionId,
                                    userId = userId,
                                    latitude = location.latitude,
                                    longitude = location.longitude
                                )
                            }
                        }

                        recordMissionTrackPointUseCase(
                            missionId = missionId,
                            userId = userId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracyMeters = location.accuracyMeters
                        )
                    }
            }
    }

    private fun startSupportObservation(
        missionId: String,
        userId: String
    ) {

        supportObservationJob?.cancel()
        notifiedSupportRequestUserIds.clear()

        supportObservationJob =
            serviceScope.launch {

                observeMissionParticipantsUseCase(
                    missionId =
                        missionId
                )
                    .catch {
                    }
                    .collect { participants ->

                        processSupportRequests(
                            missionId =
                                missionId,
                            currentUserId =
                                userId,
                            participants =
                                participants
                        )
                    }
            }
    }

    private suspend fun processSupportRequests(
        missionId: String,
        currentUserId: String,
        participants: List<MissionParticipantState>
    ) {

        val currentParticipant =
            participants
                .firstOrNull {
                    it.userId ==
                            currentUserId
                }

        if (
            currentParticipant?.isSupport !=
            true
        ) {

            notifiedSupportRequestUserIds.clear()
            return
        }

        val supportGroupId =
            currentParticipant.groupId
                ?: run {

                    notifiedSupportRequestUserIds.clear()
                    return
                }

        val activeRequests =
            participants
                .filter { participant ->

                    participant.userId !=
                            currentUserId &&
                            participant.groupId ==
                            supportGroupId &&
                            participant.status ==
                            MissionParticipantStatus
                                .NEEDS_SUPPORT
                }

        val activeRequestUserIds =
            activeRequests
                .map {
                    it.userId
                }
                .toSet()

        notifiedSupportRequestUserIds
            .retainAll(
                activeRequestUserIds
            )

        activeRequests
            .forEach { participant ->

                val shouldNotify =
                    notifiedSupportRequestUserIds
                        .add(
                            participant.userId
                        )

                if (
                    shouldNotify
                ) {

                    showSupportRequestNotification(
                        missionId =
                            missionId,
                        participant =
                            participant
                    )
                }
            }
    }

    private suspend fun showSupportRequestNotification(
        missionId: String,
        participant: MissionParticipantState
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        val profile =
            getUserByIdUseCase(
                userId =
                    participant.userId
            )
                .getOrNull()

        val participantName =
            profile
                ?.let {
                    listOf(
                        it.firstName,
                        it.lastName
                    )
                        .filter { part ->
                            part.isNotBlank()
                        }
                        .joinToString(
                            separator =
                                " "
                        )
                }
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: getString(
                    R.string
                        .mission_support_notification_unknown_user
                )

        val contentIntent =
            Intent(
                this,
                MainActivity::class.java
            ).apply {

                action =
                    ACTION_OPEN_SUPPORT_REQUEST

                putExtra(
                    EXTRA_SUPPORT_MISSION_ID,
                    missionId
                )

                putExtra(
                    EXTRA_SUPPORT_USER_ID,
                    participant.userId
                )

                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val notificationId =
            supportNotificationId(
                participant.userId
            )

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                notificationId,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val text =
            getString(
                R.string
                    .mission_support_notification_text,
                participantName
            )

        val notification =
            NotificationCompat
                .Builder(
                    this,
                    SUPPORT_NOTIFICATION_CHANNEL_ID
                )
                .setSmallIcon(
                    R.mipmap.ic_launcher
                )
                .setContentTitle(
                    getString(
                        R.string
                            .mission_support_notification_title
                    )
                )
                .setContentText(
                    text
                )
                .setStyle(
                    NotificationCompat
                        .BigTextStyle()
                        .bigText(
                            text
                        )
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setCategory(
                    NotificationCompat.CATEGORY_ALARM
                )
                .setAutoCancel(
                    true
                )
                .setContentIntent(
                    pendingIntent
                )
                .build()


        NotificationManagerCompat
            .from(
                this
            )
            .notify(
                notificationId,
                notification
            )
    }

    private fun startPendingSyncLoop() {

        syncJob?.cancel()

        syncJob =
            serviceScope.launch {
                while (isActive) {
                    runCatching {
                        syncPendingMissionTrackPointsUseCase()
                    }

                    delay(
                        30.seconds
                    )
                }
            }
    }

    private fun stopTracking() {

        trackingJob?.cancel()
        trackingJob = null

        syncJob?.cancel()
        syncJob = null

        supportObservationJob?.cancel()
        supportObservationJob = null

        notifiedSupportRequestUserIds.clear()

        activeMissionId = null
        activeUserId = null

        ServiceCompat.stopForeground(
            this,
            ServiceCompat.STOP_FOREGROUND_REMOVE
        )
    }

    private fun createLocationNotificationChannel() {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        val channel =
            NotificationChannel(
                LOCATION_NOTIFICATION_CHANNEL_ID,
                getString(
                    R.string
                        .mission_location_notification_channel_name
                ),
                NotificationManager.IMPORTANCE_LOW
            ).apply {

                description =
                    getString(
                        R.string
                            .mission_location_notification_channel_description
                    )
            }

        manager.createNotificationChannel(
            channel
        )
    }

    private fun createSupportNotificationChannel() {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.O
        ) {
            return
        }

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        val channel =
            NotificationChannel(
                SUPPORT_NOTIFICATION_CHANNEL_ID,
                getString(
                    R.string
                        .mission_support_notification_channel_name
                ),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description =
                    getString(
                        R.string
                            .mission_support_notification_channel_description
                    )

                enableVibration(
                    true
                )
            }

        manager.createNotificationChannel(
            channel
        )
    }

    private fun supportNotificationId(
        userId: String
    ): Int {

        return SUPPORT_NOTIFICATION_ID_BASE +
                (
                        userId
                            .hashCode() and
                                0x0000FFFF
                        )
    }

    companion object {

        const val ACTION_STOP =
            "br.com.ide.action.STOP_MISSION_LOCATION"

        const val ACTION_OPEN_SUPPORT_REQUEST =
            "br.com.ide.action.OPEN_SUPPORT_REQUEST"

        const val EXTRA_SUPPORT_MISSION_ID =
            "supportMissionId"

        const val EXTRA_SUPPORT_USER_ID =
            "supportUserId"

        private const val EXTRA_MISSION_ID =
            "missionId"

        private const val EXTRA_USER_ID =
            "userId"

        private const val LOCATION_NOTIFICATION_CHANNEL_ID =
            "mission_location"

        private const val SUPPORT_NOTIFICATION_CHANNEL_ID =
            "mission_support_requests"

        private const val LOCATION_NOTIFICATION_ID =
            2301

        private const val SUPPORT_NOTIFICATION_ID_BASE =
            3100

        fun createStartIntent(
            context: Context,
            missionId: String,
            userId: String
        ): Intent {

            return Intent(
                context,
                MissionLocationService::class.java
            ).apply {

                putExtra(
                    EXTRA_MISSION_ID,
                    missionId
                )

                putExtra(
                    EXTRA_USER_ID,
                    userId
                )
            }
        }
    }
}
