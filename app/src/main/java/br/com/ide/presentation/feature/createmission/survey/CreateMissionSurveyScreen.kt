package br.com.ide.presentation.feature.createmission.survey

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import br.com.ide.presentation.feature.createmission.components.CreateMissionHeader
import br.com.ide.presentation.feature.createmission.components.MissionFieldError
import br.com.ide.presentation.feature.createmission.components.MissionScreenContainer

@Composable
fun CreateMissionSurveyScreen(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit
) {
    MissionScreenContainer {

        CreateMissionHeader(
            title =
                stringResource(
                    R.string.create_mission_survey_title
                ),
            backContentDescription =
                stringResource(
                    R.string.create_mission_back
                ),
            onBackClick = {
                onEvent(
                    CreateMissionEvent.CloseSurvey
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        IdeScreenSubtitle(
            text =
                stringResource(
                    R.string.create_mission_survey_subtitle
                )
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        uiState.surveyQuestions
            .forEachIndexed {
                    index,
                    question ->

                SurveyQuestionCard(
                    number =
                        index + 1,
                    question =
                        question,
                    canRemove =
                        uiState.surveyQuestions.size > 1,
                    onEvent =
                        onEvent
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )
            }

        uiState.surveyError
            ?.let { errorRes ->

                MissionFieldError(
                    errorRes =
                        errorRes
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )
            }

        TextButton(
            onClick = {
                onEvent(
                    CreateMissionEvent.AddSurveyQuestion
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Text(
                text =
                    "+ " +
                            stringResource(
                                R.string.create_mission_add_question
                            )
            )
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        IdePrimaryButton(
            text =
                stringResource(
                    R.string.create_mission_save_survey
                ),
            onClick = {
                onEvent(
                    CreateMissionEvent.SaveSurvey
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}