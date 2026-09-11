package br.com.ide.presentation.feature.createmission.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MissionFieldError(
    errorRes: Int
) {
    Text(
        text =
            stringResource(
                errorRes
            ),
        style =
            MaterialTheme.typography.bodySmall,
        color =
            MaterialTheme.colorScheme.error,
        modifier =
            Modifier.padding(
                start = 16.dp
            )
    )
}