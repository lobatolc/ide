package br.com.ide.presentation.components.map

import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.model.UserRole

data class MissionParticipantMarker(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val groupId: String? = null,
    val groupName: String? = null,
    val colorHex: String? = null,
    val status: MissionParticipantStatus =
        MissionParticipantStatus.ACTIVE,
    val isSupport: Boolean = false,
    val isCurrentUser: Boolean = false,
    val displayName: String = "",
    val role: UserRole? = null
)
