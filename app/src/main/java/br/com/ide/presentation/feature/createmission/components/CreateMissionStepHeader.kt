package br.com.ide.presentation.feature.createmission.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle

@Composable
fun CreateMissionStepHeader(
    stepText: String,
    title: String,
    subtitle: String
) {
    Text(
        text =
            stepText,
        style =
            MaterialTheme.typography.labelLarge,
        color =
            MaterialTheme.colorScheme.primary,
        fontWeight =
            FontWeight.SemiBold
    )

    Spacer(
        modifier =
            Modifier.height(4.dp)
    )

    IdeScreenTitle(
        text =
            title
    )

    Spacer(
        modifier =
            Modifier.height(4.dp)
    )

    IdeScreenSubtitle(
        text =
            subtitle
    )
}