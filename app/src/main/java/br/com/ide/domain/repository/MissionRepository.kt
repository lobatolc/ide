package br.com.ide.domain.repository

import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionArea
import br.com.ide.domain.model.MissionGroup
import br.com.ide.domain.model.MissionLocation
import br.com.ide.domain.model.MissionStatus

interface MissionRepository {

    suspend fun getMissions():
            List<Mission>

    suspend fun getMissionById(
        missionId: String
    ): Mission?

    suspend fun createMission(
        mission: Mission
    ): Result<String>

    suspend fun updateMission(
        mission: Mission
    ): Result<Unit>

    suspend fun updateMissionLocations(
        missionId: String,
        departureLocation: MissionLocation,
        returnLocation: MissionLocation?
    ): Result<Unit>

    suspend fun updateMissionGroups(
        missionId: String,
        groups: List<MissionGroup>
    ): Result<Unit>

    suspend fun updateMissionArea(
        missionId: String,
        area: MissionArea?
    ): Result<Unit>

    suspend fun updateMissionStatus(
        missionId: String,
        status: MissionStatus
    ): Result<Unit>

    suspend fun startMission(
        missionId: String
    ): Result<Unit>

    suspend fun finishMission(
        missionId: String
    ): Result<Unit>
}
