package br.com.ide.data.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import br.com.ide.domain.location.MissionLocationServiceController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidMissionLocationServiceController @Inject constructor(
    @ApplicationContext
    private val context: Context
) : MissionLocationServiceController {

    override fun start(
        missionId: String,
        userId: String
    ) {

        ContextCompat.startForegroundService(
            context,
            MissionLocationService.createStartIntent(
                context = context,
                missionId = missionId,
                userId = userId
            )
        )
    }

    override fun stop() {

        context.startService(
            Intent(
                context,
                MissionLocationService::class.java
            ).apply {
                action = MissionLocationService.ACTION_STOP
            }
        )
    }
}
