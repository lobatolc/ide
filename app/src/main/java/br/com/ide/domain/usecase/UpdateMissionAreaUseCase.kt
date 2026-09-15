package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionArea
import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class UpdateMissionAreaUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository
) {

    suspend operator fun invoke(
        missionId: String,
        area: MissionArea?
    ): Result<Unit> {

        return missionRepository
            .updateMissionArea(
                missionId =
                    missionId,
                area =
                    area
            )
    }
}