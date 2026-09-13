package br.com.ide.presentation.feature.createmission.survey

import br.com.ide.domain.model.SurveyQuestionDraft
import br.com.ide.domain.model.SurveyQuestionType
import java.util.UUID
import javax.inject.Inject

class SurveyReducer @Inject constructor() {

    fun createEmptyQuestion():
            SurveyQuestionDraft {

        return SurveyQuestionDraft(
            id =
                UUID.randomUUID()
                    .toString()
        )
    }

    fun addQuestion(
        questions: List<SurveyQuestionDraft>
    ): List<SurveyQuestionDraft> {

        return questions +
                createEmptyQuestion()
    }

    fun removeQuestion(
        questions: List<SurveyQuestionDraft>,
        questionId: String
    ): List<SurveyQuestionDraft> {

        return questions
            .filterNot {
                it.id ==
                        questionId
            }
    }

    fun updateQuestionText(
        questions: List<SurveyQuestionDraft>,
        questionId: String,
        value: String
    ): List<SurveyQuestionDraft> {

        return questions
            .map { question ->

                if (
                    question.id ==
                    questionId
                ) {
                    question.copy(
                        question =
                            value
                    )
                } else {
                    question
                }
            }
    }

    fun updateQuestionType(
        questions: List<SurveyQuestionDraft>,
        questionId: String,
        type: SurveyQuestionType
    ): List<SurveyQuestionDraft> {

        return questions
            .map { question ->

                if (
                    question.id !=
                    questionId
                ) {
                    question

                } else {

                    when (type) {

                        SurveyQuestionType.TEXT -> {

                            question.copy(
                                type =
                                    type,
                                options =
                                    emptyList()
                            )
                        }

                        SurveyQuestionType.SINGLE_CHOICE -> {

                            val options =
                                if (
                                    question.options
                                        .size >= 2
                                ) {
                                    question.options
                                } else {
                                    listOf(
                                        "",
                                        ""
                                    )
                                }

                            question.copy(
                                type =
                                    type,
                                options =
                                    options
                            )
                        }
                    }
                }
            }
    }

    fun addOption(
        questions: List<SurveyQuestionDraft>,
        questionId: String
    ): List<SurveyQuestionDraft> {

        return questions
            .map { question ->

                if (
                    question.id ==
                    questionId &&
                    question.type ==
                    SurveyQuestionType
                        .SINGLE_CHOICE
                ) {

                    question.copy(
                        options =
                            question.options +
                                    ""
                    )

                } else {
                    question
                }
            }
    }

    fun updateOption(
        questions: List<SurveyQuestionDraft>,
        questionId: String,
        optionIndex: Int,
        value: String
    ): List<SurveyQuestionDraft> {

        return questions
            .map { question ->

                if (
                    question.id !=
                    questionId
                ) {
                    return@map question
                }

                if (
                    optionIndex !in
                    question.options.indices
                ) {
                    return@map question
                }

                val updatedOptions =
                    question.options
                        .toMutableList()

                updatedOptions[
                    optionIndex
                ] = value

                question.copy(
                    options =
                        updatedOptions
                )
            }
    }

    fun removeOption(
        questions: List<SurveyQuestionDraft>,
        questionId: String,
        optionIndex: Int
    ): List<SurveyQuestionDraft> {

        return questions
            .map { question ->

                if (
                    question.id !=
                    questionId
                ) {
                    return@map question
                }

                if (
                    optionIndex !in
                    question.options.indices
                ) {
                    return@map question
                }

                val updatedOptions =
                    question.options
                        .toMutableList()

                updatedOptions.removeAt(
                    optionIndex
                )

                question.copy(
                    options =
                        updatedOptions
                )
            }
    }
}