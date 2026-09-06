package br.com.ide.presentation.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onMissionClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage
                )

                Button(
                    onClick = {
                        onEvent(HomeEvent.Retry)
                    }
                ) {
                    Text("Tentar novamente")
                }
            }

            else -> {
                Text(
                    text = "Olá, ${uiState.userName}"
                )

                uiState.activeMissionName?.let { missionName ->
                    Text(
                        text = "Missão ativa: $missionName"
                    )
                }

                Button(
                    onClick = onMissionClick
                ) {
                    Text("Abrir missão")
                }
            }
        }
    }
}