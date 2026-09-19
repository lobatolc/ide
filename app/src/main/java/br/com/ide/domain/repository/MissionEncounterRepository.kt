package br.com.ide.domain.repository

import br.com.ide.domain.model.MissionEncounter
import kotlinx.coroutines.flow.Flow

interface MissionEncounterRepository {

    suspend fun createEncounter(
        encounter: MissionEncounter
    ): Result<String>

    suspend fun getMissionEncounters(
        missionId: String
    ): Result<List<MissionEncounter>>

    fun observeMissionEncounters(
        missionId: String
    ): Flow<List<MissionEncounter>>
}
