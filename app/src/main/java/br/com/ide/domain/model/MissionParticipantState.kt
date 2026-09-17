package br.com.ide.domain.model

import java.time.LocalDateTime

data class MissionParticipantState(
    val userId: String,
    val missionId: String,
    val groupId: String? = null,
    val isSupport: Boolean = false,

    val status: MissionParticipantStatus =
        MissionParticipantStatus.ACTIVE,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationUpdatedAt: LocalDateTime? = null,

    val joinedAt: LocalDateTime? = null,

    val supportRequestedAt: LocalDateTime? = null,

    val endedAt: LocalDateTime? = null
)
