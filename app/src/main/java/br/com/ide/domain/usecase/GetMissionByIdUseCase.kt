package br.com.ide.domain.usecase

import br.com.ide.domain.model.Mission
import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class GetMissionByIdUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository
) {

    suspend operator fun invoke(
        missionId: String
    ): Mission? {

        return missionRepository
            .getMissionById(
                missionId
            )
    }
}