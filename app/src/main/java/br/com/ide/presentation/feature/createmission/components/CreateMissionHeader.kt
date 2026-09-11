package br.com.ide.presentation.feature.createmission.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdeScreenTitle

@Composable
fun CreateMissionHeader(
    title: String,
    backContentDescription: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        IdeBackButton(
            onClick =
                onBackClick,
            contentDescription =
                backContentDescription
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        IdeScreenTitle(
            text =
                title
        )
    }
}