package br.com.ide.presentation.mapper

import androidx.annotation.StringRes
import br.com.ide.R
import br.com.ide.domain.model.MissionActivityType

@StringRes
fun MissionActivityType.toStringRes(): Int {

    return when (this) {

        MissionActivityType.VISIT ->
            R.string.create_mission_action_visit

        MissionActivityType.PRAYER ->
            R.string.create_mission_action_prayer

        MissionActivityType.BIBLE_STUDY ->
            R.string.create_mission_action_bible_study

        MissionActivityType.MATERIAL_DELIVERY ->
            R.string.create_mission_action_material_delivery

        MissionActivityType.OPINION_SURVEY ->
            R.string.create_mission_action_opinion_survey

        MissionActivityType.OTHER ->
            R.string.create_mission_action_other
    }
}