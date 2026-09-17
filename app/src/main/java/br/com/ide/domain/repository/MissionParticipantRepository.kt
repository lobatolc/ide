package br.com.ide.domain.repository

import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.MissionParticipantStatus
import kotlinx.coroutines.flow.Flow

interface MissionParticipantRepository {

    fun observeParticipants(
        missionId: String
    ): Flow<List<MissionParticipantState>>

    suspend fun getParticipants(
        missionId: String
    ): Result<List<MissionParticipantState>>

    suspend fun getParticipant(
        missionId: String,
        userId: String
    ): Result<MissionParticipantState?>

    suspend fun saveParticipant(
        participant: MissionParticipantState
    ): Result<Unit>

    suspend fun saveParticipants(
        participants: List<MissionParticipantState>
    ): Result<Unit>

    suspend fun initializeParticipants(
        participants: List<MissionParticipantState>
    ): Result<Unit>

    suspend fun updateLocation(
        missionId: String,
        userId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit>

    suspend fun updateStatus(
        missionId: String,
        userId: String,
        status: MissionParticipantStatus
    ): Result<Unit>
}
