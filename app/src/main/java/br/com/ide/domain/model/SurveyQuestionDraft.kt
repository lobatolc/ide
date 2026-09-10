package br.com.ide.domain.model

data class SurveyQuestionDraft(
    val id: String,
    val question: String = "",
    val type: SurveyQuestionType =
        SurveyQuestionType.TEXT,
    val options: List<String> =
        emptyList()
)