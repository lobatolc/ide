package br.com.ide.presentation.mapper

import androidx.annotation.StringRes
import br.com.ide.R
import br.com.ide.domain.model.MissionMaterialType

@StringRes
fun MissionMaterialType.toStringRes(): Int {

    return when (this) {

        MissionMaterialType.BOOK ->
            R.string.create_mission_material_book

        MissionMaterialType.MAGAZINE ->
            R.string.create_mission_material_magazine

        MissionMaterialType.LEAFLET ->
            R.string.create_mission_material_leaflet

        MissionMaterialType.OTHER ->
            R.string.create_mission_material_other
    }
}