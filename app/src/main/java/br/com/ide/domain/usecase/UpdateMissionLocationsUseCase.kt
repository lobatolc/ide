package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionLocation
import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class UpdateMissionLocationsUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository
) {

    suspend operator fun invoke(
        missionId: String,
        departureLocation: MissionLocation,
        returnLocation: MissionLocation?
    ): Result<Unit> {

        return missionRepository
            .updateMissionLocations(
                missionId =
                    missionId,
                departureLocation =
                    departureLocation,
                returnLocation =
                    returnLocation
            )
    }
}