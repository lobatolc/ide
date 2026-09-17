package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.repository.MissionParticipantRepository
import javax.inject.Inject

class SaveMissionParticipantsUseCase @Inject constructor(
    private val missionParticipantRepository:
    MissionParticipantRepository
) {

    suspend operator fun invoke(
        participants: List<MissionParticipantState>
    ): Result<Unit> {

        return missionParticipantRepository
            .saveParticipants(
                participants =
                    participants
            )
    }
}
