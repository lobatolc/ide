package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.repository.MissionParticipantRepository
import javax.inject.Inject

class GetMissionParticipantsUseCase @Inject constructor(
    private val missionParticipantRepository:
    MissionParticipantRepository
) {

    suspend operator fun invoke(
        missionId: String
    ): Result<List<MissionParticipantState>> {

        return missionParticipantRepository
            .getParticipants(
                missionId =
                    missionId
            )
    }
}
