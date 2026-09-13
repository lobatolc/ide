package br.com.ide.domain.model

data class MissionLocationDraft(
    val name: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)