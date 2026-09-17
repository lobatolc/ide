package br.com.ide.domain.usecase

import br.com.ide.domain.repository.MissionTrackLocalRepository
import br.com.ide.domain.repository.MissionTrackRemoteRepository
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull

class SyncPendingMissionTrackPointsUseCase @Inject constructor(
    private val localRepository: MissionTrackLocalRepository,
    private val remoteRepository: MissionTrackRemoteRepository
) {

    suspend operator fun invoke() {

        val pending =
            localRepository.getPending(
                limit = BATCH_SIZE
            )

        for (point in pending) {
            val result =
                withTimeoutOrNull(
                    UPLOAD_TIMEOUT_MILLIS
                ) {
                    remoteRepository.save(point)
                }

            if (result?.isSuccess == true) {
                localRepository.markSynced(point.id)
            } else {
                break
            }
        }
    }

    private companion object {
        const val BATCH_SIZE = 100
        const val UPLOAD_TIMEOUT_MILLIS = 5_000L
    }
}
