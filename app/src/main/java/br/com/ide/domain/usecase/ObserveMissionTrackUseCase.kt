package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionTrackPoint
import br.com.ide.domain.repository.MissionTrackRemoteRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveMissionTrackUseCase @Inject constructor(
    private val repository:
    MissionTrackRemoteRepository
) {

    operator fun invoke(
        missionId: String,
        userId: String
    ): Flow<List<MissionTrackPoint>> {

        return repository
            .observeTrack(
                missionId =
                    missionId,
                userId =
                    userId
            )
    }
}
