package br.com.ide.presentation.mapper

import br.com.ide.R
import br.com.ide.domain.model.MissionStatus

fun MissionStatus.toStringRes(): Int {
    return when (this) {
        MissionStatus.SCHEDULED ->
            R.string.mission_status_scheduled

        MissionStatus.IN_PROGRESS ->
            R.string.mission_status_in_progress

        MissionStatus.COMPLETED ->
            R.string.mission_status_completed

        MissionStatus.CANCELLED ->
            R.string.mission_status_cancelled
    }
}