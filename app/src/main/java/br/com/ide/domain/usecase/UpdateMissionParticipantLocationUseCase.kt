package br.com.ide.domain.usecase

import br.com.ide.domain.repository.MissionParticipantRepository
import javax.inject.Inject

class UpdateMissionParticipantLocationUseCase @Inject constructor(
    private val missionParticipantRepository:
    MissionParticipantRepository
) {

    suspend operator fun invoke(
        missionId: String,
        userId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> {

        return missionParticipantRepository
            .updateLocation(
                missionId =
                    missionId,
                userId =
                    userId,
                latitude =
                    latitude,
                longitude =
                    longitude
            )
    }
}
