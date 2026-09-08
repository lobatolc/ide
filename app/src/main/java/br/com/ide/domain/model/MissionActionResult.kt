package br.com.ide.domain.model

data class MissionActionResult(
    val id: String,
    val missionId: String,
    val actionId: String,
    val missionaryId: String,
    val quantity: Int
)