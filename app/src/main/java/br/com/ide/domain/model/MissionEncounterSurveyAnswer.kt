package br.com.ide.domain.model

data class MissionEncounterSurveyAnswer(
    val questionId: String,
    val question: String,
    val type: SurveyQuestionType,
    val answer: String
)
