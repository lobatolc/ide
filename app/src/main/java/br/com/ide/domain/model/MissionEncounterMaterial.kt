package br.com.ide.domain.model

data class MissionEncounterMaterial(
    val type: MissionMaterialType,
    val customName: String? = null,
    val quantity: Int
)
