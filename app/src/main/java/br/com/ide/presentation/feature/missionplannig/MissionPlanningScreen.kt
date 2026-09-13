package br.com.ide.presentation.feature.missionplanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MissionPlanningScreen(
    missionId: String,
    onBackClick: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    16.dp
                )
    ) {

        IconButton(
            onClick =
                onBackClick
        ) {

            Icon(
                imageVector =
                    Icons.AutoMirrored
                        .Outlined
                        .ArrowBack,
                contentDescription =
                    null
            )
        }

        Text(
            text =
                "Planejamento da missão",
            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )

        Text(
            text =
                "Missão: $missionId",
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}