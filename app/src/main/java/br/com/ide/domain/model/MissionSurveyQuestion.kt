package br.com.ide.domain.model

data class MissionSurveyQuestion(
    val id: String,
    val question: String,
    val type: SurveyQuestionType,
    val options: List<String> = emptyList()
)