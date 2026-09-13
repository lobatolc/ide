package br.com.ide.presentation.feature.createmission

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.luminance
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.presentation.feature.createmission.actions.CreateMissionActionsScreen
import br.com.ide.presentation.feature.createmission.general.CreateMissionGeneralScreen
import br.com.ide.presentation.feature.createmission.materials.CreateMissionMaterialsScreen
import br.com.ide.presentation.feature.createmission.participants.CreateMissionParticipantsScreen
import br.com.ide.presentation.feature.createmission.summary.CreateMissionSummaryScreen
import br.com.ide.presentation.feature.createmission.survey.CreateMissionSurveyScreen

@Composable
fun CreateMissionScreen(
    onBackClick: () -> Unit,
    viewModel: CreateMissionViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    val isDarkTheme =
        MaterialTheme.colorScheme.background
            .luminance() < 0.5f

    when (uiState.subScreen) {

        CreateMissionSubScreen.MATERIALS -> {
            CreateMissionMaterialsScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }

        CreateMissionSubScreen.SURVEY -> {
            CreateMissionSurveyScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }

        CreateMissionSubScreen.NONE -> {

            when (uiState.currentStep) {

                1 -> {
                    CreateMissionGeneralScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onBackClick = {

                            if (
                                uiState.editingFromSummary
                            ) {

                                viewModel.onEvent(
                                    CreateMissionEvent.ReturnToSummary
                                )

                            } else {

                                onBackClick()
                            }
                        }
                    )
                }

                2 -> {
                    CreateMissionParticipantsScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent
                    )
                }

                3 -> {
                    CreateMissionActionsScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent
                    )
                }

                4 -> {
                    CreateMissionSummaryScreen(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onCreateMissionClick = {
                            /*
                             * Próximo passo:
                             * salvar a missão no Firestore.
                             */
                        }
                    )
                }

            }
        }
    }
}