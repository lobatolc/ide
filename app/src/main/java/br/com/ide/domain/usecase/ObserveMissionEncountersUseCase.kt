package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.repository.MissionEncounterRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveMissionEncountersUseCase @Inject constructor(
    private val repository: MissionEncounterRepository
) {

    operator fun invoke(
        missionId: String
    ): Flow<List<MissionEncounter>> =
        repository.observeMissionEncounters(
            missionId = missionId
        )
}
