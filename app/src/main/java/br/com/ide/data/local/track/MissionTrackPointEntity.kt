package br.com.ide.data.local.track

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mission_track_points",
    indices = [
        Index(value = ["missionId", "userId", "capturedAtEpochMillis"]),
        Index(value = ["synced", "capturedAtEpochMillis"])
    ]
)
data class MissionTrackPointEntity(
    @PrimaryKey val id: String,
    val missionId: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val capturedAtEpochMillis: Long,
    val synced: Boolean = false
)
