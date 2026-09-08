package br.com.ide.domain.repository

import br.com.ide.domain.model.Mission

interface MissionRepository {

    suspend fun getMissions(): List<Mission>

    suspend fun getMissionById(
        missionId: String
    ): Mission?

    suspend fun createMission(
        mission: Mission
    ): Result<Unit>

    suspend fun updateMission(
        mission: Mission
    ): Result<Unit>
}