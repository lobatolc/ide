package br.com.ide.domain.model

data class CoveredStreet(
    val id: String,
    val missionId: String,
    val teamId: String?,
    val missionaryId: String?,
    val streetName: String,
    val distanceMeters: Double? = null
)