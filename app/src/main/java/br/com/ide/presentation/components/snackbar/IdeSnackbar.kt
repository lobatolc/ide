package br.com.ide.presentation.components.snackbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun IdeSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->

        val visuals =
            snackbarData.visuals
                    as? IdeSnackbarVisuals

        val type =
            visuals?.type
                ?: IdeSnackbarType.INFO

        val isDarkTheme =
            MaterialTheme
                .colorScheme
                .background
                .luminance() < 0.5f

        val containerColor =
            snackbarContainerColor(
                type = type,
                isDarkTheme = isDarkTheme
            )

        val contentColor =
            snackbarContentColor(
                type = type
            )

        val icon =
            when (type) {

                IdeSnackbarType.SUCCESS ->
                    Icons.Outlined.CheckCircle

                IdeSnackbarType.ERROR ->
                    Icons.Outlined.ErrorOutline

                IdeSnackbarType.WARNING ->
                    Icons.Outlined.WarningAmber

                IdeSnackbarType.INFO ->
                    Icons.Outlined.Info
            }

        Snackbar(
            containerColor =
                containerColor,
            contentColor =
                contentColor
        ) {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                Icon(
                    imageVector =
                        icon,
                    contentDescription =
                        null,
                    tint =
                        contentColor
                )

                Text(
                    text =
                        snackbarData
                            .visuals
                            .message,
                    color =
                        contentColor,
                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                )
            }
        }
    }
}

private fun snackbarContainerColor(
    type: IdeSnackbarType,
    isDarkTheme: Boolean
): Color {

    return when (type) {

        IdeSnackbarType.SUCCESS ->
            if (isDarkTheme) {
                IdeSnackbarColors.SuccessDark
            } else {
                IdeSnackbarColors.SuccessLight
            }

        IdeSnackbarType.ERROR ->
            if (isDarkTheme) {
                IdeSnackbarColors.ErrorDark
            } else {
                IdeSnackbarColors.ErrorLight
            }

        IdeSnackbarType.WARNING ->
            if (isDarkTheme) {
                IdeSnackbarColors.WarningDark
            } else {
                IdeSnackbarColors.WarningLight
            }

        IdeSnackbarType.INFO ->
            if (isDarkTheme) {
                IdeSnackbarColors.InfoDark
            } else {
                IdeSnackbarColors.InfoLight
            }
    }
}

private fun snackbarContentColor(
    type: IdeSnackbarType
): Color {

    return when (type) {

        IdeSnackbarType.WARNING ->
            IdeSnackbarColors.WarningContent

        else ->
            IdeSnackbarColors.LightContent
    }
}