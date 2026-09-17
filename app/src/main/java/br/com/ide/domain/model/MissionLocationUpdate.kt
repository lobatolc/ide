package br.com.ide.domain.model

data class MissionLocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null
)
