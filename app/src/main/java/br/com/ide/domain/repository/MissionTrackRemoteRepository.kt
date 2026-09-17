package br.com.ide.domain.repository

import br.com.ide.domain.model.MissionTrackPoint
import kotlinx.coroutines.flow.Flow

interface MissionTrackRemoteRepository {

    suspend fun save(
        point: MissionTrackPoint
    ): Result<Unit>

    fun observeTrack(
        missionId: String,
        userId: String
    ): Flow<List<MissionTrackPoint>>
}
