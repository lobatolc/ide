package br.com.ide.domain.model

data class MissionArea(
    val polygonPoints: List<MissionCoordinate>
) {

    val isValid: Boolean
        get() =
            polygonPoints.size >= 3
}