package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class UpdateMissionStatusUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository
) {

    suspend operator fun invoke(
        missionId: String,
        status: MissionStatus
    ): Result<Unit> {

        return missionRepository
            .updateMissionStatus(
                missionId =
                    missionId,
                status =
                    status
            )
    }
}
