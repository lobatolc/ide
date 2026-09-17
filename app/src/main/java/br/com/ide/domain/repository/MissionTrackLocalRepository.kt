package br.com.ide.domain.repository

import br.com.ide.domain.model.MissionTrackPoint

interface MissionTrackLocalRepository {

    suspend fun save(point: MissionTrackPoint)

    suspend fun getLatest(
        missionId: String,
        userId: String
    ): MissionTrackPoint?

    suspend fun getPending(
        limit: Int = 100
    ): List<MissionTrackPoint>

    suspend fun markSynced(pointId: String)

    suspend fun getTrack(
        missionId: String,
        userId: String
    ): List<MissionTrackPoint>
}
