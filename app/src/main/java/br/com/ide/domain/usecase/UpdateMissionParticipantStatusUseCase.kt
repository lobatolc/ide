package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.repository.MissionParticipantRepository
import javax.inject.Inject

class UpdateMissionParticipantStatusUseCase @Inject constructor(
    private val missionParticipantRepository:
    MissionParticipantRepository
) {

    suspend operator fun invoke(
        missionId: String,
        userId: String,
        status: MissionParticipantStatus
    ): Result<Unit> {

        return missionParticipantRepository
            .updateStatus(
                missionId =
                    missionId,
                userId =
                    userId,
                status =
                    status
            )
    }
}
