package br.com.ide.presentation.feature.createmission.survey

import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionSubScreenReducer
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class SurveyEventHandler @Inject constructor(
    private val surveyReducer:
    SurveyReducer,
    private val subScreenReducer:
    CreateMissionSubScreenReducer
) {

    fun handle(
        state: CreateMissionUiState,
        event: CreateMissionEvent.Survey
    ): CreateMissionUiState {

        return when (event) {

            CreateMissionEvent.OpenSurvey -> {

                val stateWithInitialQuestion =
                    if (
                        state.surveyQuestions
                            .isEmpty()
                    ) {
                        state.copy(
                            surveyQuestions =
                                listOf(
                                    surveyReducer
                                        .createEmptyQuestion()
                                )
                        )
                    } else {
                        state
                    }

                subScreenReducer
                    .openSurvey(
                        stateWithInitialQuestion
                    )
            }

            CreateMissionEvent.CloseSurvey -> {

                subScreenReducer
                    .close(
                        state
                    )
            }

            CreateMissionEvent.AddSurveyQuestion -> {

                state.copy(
                    surveyQuestions =
                        surveyReducer.addQuestion(
                            state.surveyQuestions
                        ),
                    surveyError = null
                )
            }

            is CreateMissionEvent.RemoveSurveyQuestion -> {

                state.copy(
                    surveyQuestions =
                        surveyReducer.removeQuestion(
                            questions =
                                state.surveyQuestions,
                            questionId =
                                event.questionId
                        ),
                    surveyError = null
                )
            }

            is CreateMissionEvent.SurveyQuestionChanged -> {

                state.copy(
                    surveyQuestions =
                        surveyReducer.updateQuestionText(
                            questions =
                                state.surveyQuestions,
                            questionId =
                                event.questionId,
                            value =
                                event.value
                        ),
                    surveyError = null
                )
            }

            is CreateMissionEvent.SurveyQuestionTypeChanged -> {

                state.copy(
                    surveyQuestions =
                        surveyReducer.updateQuestionType(
                            questions =
                                state.surveyQuestions,
                            questionId =
                                event.questionId,
                            type =
                                event.type
                        ),
                    surveyError = null
                )
            }

            is CreateMissionEvent.AddSurveyOption -> {

                state.copy(
                    surveyQuestions =
                        surveyReducer.addOption(
                            questions =
                                state.surveyQuestions,
                            questionId =
                                event.questionId
                        ),
                    surveyError = null
                )
            }

            is CreateMissionEvent.SurveyOptionChanged -> {

                state.copy(
                    surveyQuestions =
                        surveyReducer.updateOption(
                            questions =
                                state.surveyQuestions,
                            questionId =
                                event.questionId,
                            optionIndex =
                                event.optionIndex,
                            value =
                                event.value
                        ),
                    surveyError = null
                )
            }

            is CreateMissionEvent.RemoveSurveyOption -> {

                state.copy(
                    surveyQuestions =
                        surveyReducer.removeOption(
                            questions =
                                state.surveyQuestions,
                            questionId =
                                event.questionId,
                            optionIndex =
                                event.optionIndex
                        ),
                    surveyError = null
                )
            }

            CreateMissionEvent.SaveSurvey -> {
                state
            }
        }
    }
}