package br.com.ide.presentation.feature.createmission.survey

import br.com.ide.R
import br.com.ide.domain.model.SurveyQuestionDraft
import br.com.ide.domain.model.SurveyQuestionType
import javax.inject.Inject

class SurveyValidator @Inject constructor() {

    fun validate(
        questions: List<SurveyQuestionDraft>
    ): Int? {

        return when {

            questions.isEmpty() -> {
                R.string
                    .create_mission_survey_required
            }

            questions.any {
                it.question
                    .isBlank()
            } -> {
                R.string
                    .create_mission_survey_question_required
            }

            questions.any {
                it.type ==
                        SurveyQuestionType.SINGLE_CHOICE &&
                        it.options
                            .count { option ->
                                option.isNotBlank()
                            } < 2
            } -> {
                R.string
                    .create_mission_survey_options_required
            }

            else -> {
                null
            }
        }
    }

    fun isValid(
        questions: List<SurveyQuestionDraft>
    ): Boolean {
        return validate(questions) == null
    }
}