package br.com.ide.data.repository

import br.com.ide.data.local.track.MissionTrackPointDao
import br.com.ide.data.local.track.MissionTrackPointEntity
import br.com.ide.domain.model.MissionTrackPoint
import br.com.ide.domain.repository.MissionTrackLocalRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMissionTrackLocalRepository @Inject constructor(
    private val dao: MissionTrackPointDao
) : MissionTrackLocalRepository {

    override suspend fun save(point: MissionTrackPoint) {
        dao.insert(point.toEntity())
    }

    override suspend fun getLatest(
        missionId: String,
        userId: String
    ): MissionTrackPoint? {
        return dao.getLatest(missionId, userId)?.toDomain()
    }

    override suspend fun getPending(
        limit: Int
    ): List<MissionTrackPoint> {
        return dao.getPending(limit).map(MissionTrackPointEntity::toDomain)
    }

    override suspend fun markSynced(pointId: String) {
        dao.markSynced(pointId)
    }

    override suspend fun getTrack(
        missionId: String,
        userId: String
    ): List<MissionTrackPoint> {
        return dao.getTrack(missionId, userId).map(MissionTrackPointEntity::toDomain)
    }
}

private fun MissionTrackPoint.toEntity() =
    MissionTrackPointEntity(
        id = id,
        missionId = missionId,
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        capturedAtEpochMillis = capturedAtEpochMillis,
        synced = false
    )

private fun MissionTrackPointEntity.toDomain() =
    MissionTrackPoint(
        id = id,
        missionId = missionId,
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        capturedAtEpochMillis = capturedAtEpochMillis
    )
