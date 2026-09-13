package br.com.ide.presentation.components.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

data class IdeSnackbarVisuals(
    override val message: String,
    val type: IdeSnackbarType,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration =
        SnackbarDuration.Short
) : SnackbarVisuals