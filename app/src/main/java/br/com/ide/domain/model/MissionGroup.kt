package br.com.ide.domain.model

data class MissionGroup(
    val id: String,
    val name: String,
    val colorHex: String,
    val participantIds: List<String>,
    val supportUserId: String? = null
)