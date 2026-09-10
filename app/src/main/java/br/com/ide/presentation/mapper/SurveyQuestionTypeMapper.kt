package br.com.ide.presentation.mapper

import androidx.annotation.StringRes
import br.com.ide.R
import br.com.ide.domain.model.SurveyQuestionType

@StringRes
fun SurveyQuestionType.toStringRes(): Int {

    return when (this) {

        SurveyQuestionType.TEXT ->
            R.string.create_mission_survey_type_text

        SurveyQuestionType.SINGLE_CHOICE ->
            R.string.create_mission_survey_type_single_choice
    }
}