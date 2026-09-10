package br.com.ide.presentation.mapper

import androidx.annotation.StringRes
import br.com.ide.R
import br.com.ide.domain.model.MissionMovement

@StringRes
fun MissionMovement.toStringRes(): Int {

    return when (this) {

        MissionMovement.BREAKING_THE_SILENCE ->
            R.string
                .create_mission_movement_breaking_silence

        MissionMovement.HOPE_IMPACT ->
            R.string
                .create_mission_movement_hope_impact

        MissionMovement.OTHER ->
            R.string
                .create_mission_movement_other
    }
}