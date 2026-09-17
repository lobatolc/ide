package br.com.ide.data.local.track

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MissionTrackPointDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(point: MissionTrackPointEntity)

    @Query(
        """
        SELECT *
        FROM mission_track_points
        WHERE missionId = :missionId
          AND userId = :userId
        ORDER BY capturedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun getLatest(
        missionId: String,
        userId: String
    ): MissionTrackPointEntity?

    @Query(
        """
        SELECT *
        FROM mission_track_points
        WHERE synced = 0
        ORDER BY capturedAtEpochMillis ASC
        LIMIT :limit
        """
    )
    suspend fun getPending(limit: Int): List<MissionTrackPointEntity>

    @Query(
        """
        UPDATE mission_track_points
        SET synced = 1
        WHERE id = :pointId
        """
    )
    suspend fun markSynced(pointId: String)

    @Query(
        """
        SELECT *
        FROM mission_track_points
        WHERE missionId = :missionId
          AND userId = :userId
        ORDER BY capturedAtEpochMillis ASC
        """
    )
    suspend fun getTrack(
        missionId: String,
        userId: String
    ): List<MissionTrackPointEntity>
}
