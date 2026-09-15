package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionGroup
import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class UpdateMissionGroupsUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository
) {

    suspend operator fun invoke(
        missionId: String,
        groups: List<MissionGroup>
    ): Result<Unit> {

        return missionRepository
            .updateMissionGroups(
                missionId = missionId,
                groups = groups
            )
    }
}