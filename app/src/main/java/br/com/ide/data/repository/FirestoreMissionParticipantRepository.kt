package br.com.ide.data.repository

import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.repository.MissionParticipantRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreMissionParticipantRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MissionParticipantRepository {

    override fun observeParticipants(
        missionId: String
    ): Flow<List<MissionParticipantState>> {

        return callbackFlow {

            val registration =
                participantsCollection(
                    missionId
                )
                    .addSnapshotListener {
                            snapshot,
                            error ->

                        if (
                            error != null
                        ) {
                            close(
                                error
                            )
                            return@addSnapshotListener
                        }

                        val participants =
                            snapshot
                                ?.documents
                                ?.mapNotNull { document ->
                                    document.toParticipantState(
                                        missionId =
                                            missionId
                                    )
                                }
                                ?: emptyList()

                        trySend(
                            participants
                        )
                    }

            awaitClose {
                registration.remove()
            }
        }
    }

    override suspend fun getParticipants(
        missionId: String
    ): Result<List<MissionParticipantState>> {

        return try {

            val participants =
                participantsCollection(
                    missionId
                )
                    .get()
                    .await()
                    .documents
                    .mapNotNull { document ->
                        document.toParticipantState(
                            missionId =
                                missionId
                        )
                    }

            Result.success(
                participants
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    override suspend fun getParticipant(
        missionId: String,
        userId: String
    ): Result<MissionParticipantState?> {

        return try {

            val document =
                participantsCollection(
                    missionId
                )
                    .document(
                        userId
                    )
                    .get()
                    .await()

            val participant =
                if (
                    document.exists()
                ) {
                    document.toParticipantState(
                        missionId =
                            missionId
                    )
                } else {
                    null
                }

            Result.success(
                participant
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    override suspend fun saveParticipant(
        participant: MissionParticipantState
    ): Result<Unit> {

        return try {

            participantsCollection(
                participant.missionId
            )
                .document(
                    participant.userId
                )
                .set(
                    participant.toFirestoreMap()
                )
                .await()

            Result.success(
                Unit
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    override suspend fun saveParticipants(
        participants: List<MissionParticipantState>
    ): Result<Unit> {

        if (
            participants.isEmpty()
        ) {
            return Result.success(
                Unit
            )
        }

        return try {

            val batch =
                firestore.batch()

            participants
                .forEach { participant ->

                    val document =
                        participantsCollection(
                            participant.missionId
                        )
                            .document(
                                participant.userId
                            )

                    batch.set(
                        document,
                        participant.toFirestoreMap()
                    )
                }

            batch
                .commit()
                .await()

            Result.success(
                Unit
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    override suspend fun initializeParticipants(
        participants: List<MissionParticipantState>
    ): Result<Unit> {

        if (
            participants.isEmpty()
        ) {
            return Result.success(
                Unit
            )
        }

        return try {

            firestore
                .runTransaction { transaction ->

                    val references =
                        participants
                            .associateWith { participant ->

                                participantsCollection(
                                    participant.missionId
                                )
                                    .document(
                                        participant.userId
                                    )
                            }

                    val existing =
                        references
                            .mapValues {
                                    (_, reference) ->

                                transaction
                                    .get(
                                        reference
                                    )
                                    .exists()
                            }

                    references
                        .forEach {
                                (
                                    participant,
                                    reference
                                ) ->

                            if (
                                existing[
                                    participant
                                ] != true
                            ) {

                                transaction.set(
                                    reference,
                                    participant
                                        .toInitialFirestoreMap()
                                )
                            }
                        }

                    Unit
                }
                .await()

            Result.success(
                Unit
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    override suspend fun updateLocation(
        missionId: String,
        userId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {

        return upsertParticipantFields(
            missionId =
                missionId,
            userId =
                userId,
            update =
                mapOf(
                    "latitude" to
                            latitude,
                    "longitude" to
                            longitude,
                    "locationUpdatedAt" to
                            FieldValue.serverTimestamp()
                )
        )
    }

    override suspend fun updateStatus(
        missionId: String,
        userId: String,
        status: MissionParticipantStatus
    ): Result<Unit> {

        val update =
            mutableMapOf<String, Any?>(
                "status" to
                        status.name
            )

        when (
            status
        ) {

            MissionParticipantStatus.ACTIVE -> {
                update[
                    "supportRequestedAt"
                ] = null

                update[
                    "endedAt"
                ] = null

                update[
                    "endedByMission"
                ] = false
            }

            MissionParticipantStatus.NEEDS_SUPPORT -> {
                update[
                    "supportRequestedAt"
                ] = FieldValue
                    .serverTimestamp()

                update[
                    "endedByMission"
                ] = false
            }

            MissionParticipantStatus.FINISHED -> {
                update[
                    "endedAt"
                ] = FieldValue
                    .serverTimestamp()

                update[
                    "supportRequestedAt"
                ] = null

                /*
                 * FINISHED por esta operação representa encerramento
                 * voluntário do participante. O encerramento da missão
                 * define endedByMission = true em outro fluxo.
                 */
                update[
                    "endedByMission"
                ] = false
            }
        }

        return upsertParticipantFields(
            missionId =
                missionId,
            userId =
                userId,
            update =
                update
        )
    }

    /*
     * Atualiza normalmente quando o documento existe. Se algum dado antigo
     * ou fluxo anterior deixou participants/{userId} ausente, reconstruímos
     * um estado mínimo coerente e aplicamos a atualização.
     *
     * Isso elimina o NOT_FOUND sem esconder a correção principal feita no
     * StartMissionUseCase.
     */
    private suspend fun upsertParticipantFields(
        missionId: String,
        userId: String,
        update: Map<String, Any?>
    ): Result<Unit> {

        return try {

            val participantReference =
                participantsCollection(
                    missionId
                )
                    .document(
                        userId
                    )

            val participantSnapshot =
                participantReference
                    .get()
                    .await()

            if (
                participantSnapshot.exists()
            ) {
                participantReference
                    .update(
                        update
                    )
                    .await()
            } else {

                val missionSnapshot =
                    firestore
                        .collection(
                            "missions"
                        )
                        .document(
                            missionId
                        )
                        .get()
                        .await()

                if (
                    !missionSnapshot.exists()
                ) {
                    return Result.failure(
                        IllegalStateException(
                            "Mission not found."
                        )
                    )
                }

                val groups =
                    (
                            missionSnapshot[
                                "groups"
                            ] as? List<*>
                            )
                        ?.mapNotNull {
                            it as? Map<*, *>
                        }
                        .orEmpty()

                val matchedGroup =
                    groups
                        .firstOrNull { group ->

                            val participantIds =
                                (
                                        group[
                                            "participantIds"
                                        ] as? List<*>
                                        )
                                    ?.mapNotNull {
                                        it as? String
                                    }
                                    .orEmpty()

                            val supportUserId =
                                group[
                                    "supportUserId"
                                ] as? String

                            userId in participantIds ||
                                    supportUserId ==
                                    userId
                        }

                val groupId =
                    matchedGroup
                        ?.get(
                            "id"
                        ) as? String

                val supportUserId =
                    matchedGroup
                        ?.get(
                            "supportUserId"
                        ) as? String

                val initialData =
                    mutableMapOf<String, Any?>(
                        "userId" to
                                userId,
                        "missionId" to
                                missionId,
                        "groupId" to
                                groupId,
                        "isSupport" to
                                (
                                        supportUserId ==
                                                userId
                                        ),
                        "status" to
                                MissionParticipantStatus
                                    .ACTIVE
                                    .name,
                        "latitude" to
                                null,
                        "longitude" to
                                null,
                        "locationUpdatedAt" to
                                null,
                        "joinedAt" to
                                (
                                        missionSnapshot
                                            .getTimestamp(
                                                "startedAt"
                                            )
                                            ?: FieldValue.serverTimestamp()
                                        ),
                        "supportRequestedAt" to
                                null,
                        "endedAt" to
                                null,
                        "endedByMission" to
                                false
                    )

                initialData.putAll(
                    update
                )

                participantReference
                    .set(
                        initialData
                    )
                    .await()
            }

            Result.success(
                Unit
            )

        } catch (
            exception: Exception
        ) {
            Result.failure(
                exception
            )
        }
    }

    private fun participantsCollection(
        missionId: String
    ) =
        firestore
            .collection(
                "missions"
            )
            .document(
                missionId
            )
            .collection(
                "participants"
            )

    private fun DocumentSnapshot.toParticipantState(
        missionId: String
    ): MissionParticipantState? {

        val userId =
            getString(
                "userId"
            )
                ?: id

        if (
            userId.isBlank()
        ) {
            return null
        }

        val status =
            getString(
                "status"
            )
                ?.let { value ->

                    runCatching {

                        MissionParticipantStatus
                            .valueOf(
                                value
                            )

                    }.getOrNull()
                }
                ?: MissionParticipantStatus.ACTIVE

        return MissionParticipantState(

            userId =
                userId,

            missionId =
                getString(
                    "missionId"
                )
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: missionId,

            groupId =
                getString(
                    "groupId"
                ),

            isSupport =
                getBoolean(
                    "isSupport"
                )
                    ?: false,

            status =
                status,

            latitude =
                getDouble(
                    "latitude"
                ),

            longitude =
                getDouble(
                    "longitude"
                ),

            locationUpdatedAt =
                getTimestamp(
                    "locationUpdatedAt"
                )
                    ?.toLocalDateTime(),

            joinedAt =
                getTimestamp(
                    "joinedAt"
                )
                    ?.toLocalDateTime(),

            supportRequestedAt =
                getTimestamp(
                    "supportRequestedAt"
                )
                    ?.toLocalDateTime(),

            endedAt =
                getTimestamp(
                    "endedAt"
                )
                    ?.toLocalDateTime(),

            endedByMission =
                getBoolean(
                    "endedByMission"
                )
                    ?: false
        )
    }

    private fun MissionParticipantState.toFirestoreMap():
            Map<String, Any?> {

        return mapOf(

            "userId" to
                    userId,

            "missionId" to
                    missionId,

            "groupId" to
                    groupId,

            "isSupport" to
                    isSupport,

            "status" to
                    status.name,

            "latitude" to
                    latitude,

            "longitude" to
                    longitude,

            "locationUpdatedAt" to
                    locationUpdatedAt
                        ?.toTimestamp(),

            "joinedAt" to
                    joinedAt
                        ?.toTimestamp(),

            "supportRequestedAt" to
                    supportRequestedAt
                        ?.toTimestamp(),

            "endedAt" to
                    endedAt
                        ?.toTimestamp(),

            "endedByMission" to
                    endedByMission
        )
    }

    private fun MissionParticipantState.toInitialFirestoreMap():
            Map<String, Any?> {

        return mapOf(

            "userId" to
                    userId,

            "missionId" to
                    missionId,

            "groupId" to
                    groupId,

            "isSupport" to
                    isSupport,

            "status" to
                    status.name,

            "latitude" to
                    null,

            "longitude" to
                    null,

            "locationUpdatedAt" to
                    null,

            "joinedAt" to
                    FieldValue.serverTimestamp(),

            "supportRequestedAt" to
                    null,

            "endedAt" to
                    null,

            "endedByMission" to
                    false
        )
    }

    private fun LocalDateTime.toTimestamp():
            Timestamp {

        val instant =
            atZone(
                ZoneId.systemDefault()
            )
                .toInstant()

        return Timestamp(
            instant.epochSecond,
            instant.nano
        )
    }

    private fun Timestamp.toLocalDateTime():
            LocalDateTime {

        return Instant
            .ofEpochSecond(
                seconds,
                nanoseconds.toLong()
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .toLocalDateTime()
    }
}
