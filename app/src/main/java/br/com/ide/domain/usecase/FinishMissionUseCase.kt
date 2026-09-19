package br.com.ide.domain.usecase

import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class FinishMissionUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository
) {

    suspend operator fun invoke(
        missionId: String
    ): Result<Unit> {

        return missionRepository
            .finishMission(
                missionId =
                    missionId
            )
    }
}
