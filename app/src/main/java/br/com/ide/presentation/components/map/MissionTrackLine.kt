package br.com.ide.presentation.components.map

import br.com.ide.domain.model.MissionCoordinate

data class MissionTrackLine(
    val userId: String,
    val groupId: String? = null,
    val colorHex: String? = null,
    val points: List<MissionCoordinate> =
        emptyList()
)
