package br.com.ide.domain.model

data class MissionEncounterMarker(
    val encounterId: String,
    val registeredByUserId: String,
    val groupId: String? = null,
    val colorHex: String? = null,
    val latitude: Double,
    val longitude: Double,
    val personName: String? = null
)
