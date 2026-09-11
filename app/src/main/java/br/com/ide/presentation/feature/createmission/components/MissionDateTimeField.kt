package br.com.ide.presentation.feature.createmission.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MissionDateTimeField(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    hasError: Boolean,
    errorText: String?,
    onClick: () -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                label,
            style =
                MaterialTheme.typography.labelMedium,
            color =
                if (hasError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                }
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClick()
                    },
            shape =
                RoundedCornerShape(16.dp),
            color =
                MaterialTheme.colorScheme.surface,
            border =
                BorderStroke(
                    width = 1.dp,
                    color =
                        if (hasError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                )
        ) {

            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                CompositionLocalProvider(
                    LocalContentColor provides
                            MaterialTheme.colorScheme.primary
                ) {
                    icon()
                }

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Text(
                    text =
                        value,
                    style =
                        MaterialTheme.typography.bodyLarge,
                    color =
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (
            hasError &&
            errorText != null
        ) {

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    errorText,
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
    }
}