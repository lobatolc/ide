package br.com.ide.domain.model

import java.time.LocalDateTime

data class Mission(
    val id: String,
    val name: String,
    val scheduledAt: LocalDateTime,
    val address: String,
    val description: String,
    val status: MissionStatus,
    val photoUrl: String? = null,
    val actions: List<MissionAction> = emptyList(),
    val totalDurationMinutes: Long? = null,
    val createdBy: String
)