package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.repository.MissionParticipantRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveMissionParticipantsUseCase @Inject constructor(
    private val missionParticipantRepository:
    MissionParticipantRepository
) {

    operator fun invoke(
        missionId: String
    ): Flow<List<MissionParticipantState>> {

        return missionParticipantRepository
            .observeParticipants(
                missionId =
                    missionId
            )
    }
}
