package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.repository.MissionEncounterRepository
import javax.inject.Inject

class GetMissionEncountersUseCase @Inject constructor(
    private val repository: MissionEncounterRepository
) {

    suspend operator fun invoke(
        missionId: String
    ): Result<List<MissionEncounter>> =
        repository.getMissionEncounters(
            missionId = missionId
        )
}
