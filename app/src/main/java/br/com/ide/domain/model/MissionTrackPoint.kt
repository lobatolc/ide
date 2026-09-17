package br.com.ide.domain.model

data class MissionTrackPoint(
    val id: String,
    val missionId: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
    val capturedAtEpochMillis: Long
)
