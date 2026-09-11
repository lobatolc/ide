package br.com.ide.presentation.feature.createmission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.presentation.feature.createmission.general.CreateMissionGeneralScreen
import br.com.ide.presentation.feature.createmission.participants.CreateMissionParticipantsScreen
import br.com.ide.presentation.feature.createmission.actions.CreateMissionActionsScreen
import br.com.ide.presentation.feature.createmission.materials.CreateMissionMaterialsScreen
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
                        onBackClick = onBackClick
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
            }
        }
    }
}