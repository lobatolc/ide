package br.com.ide.presentation.components.snackbar

import androidx.annotation.StringRes

data class IdeSnackbarMessage(
    @StringRes
    val messageRes: Int,
    val type: IdeSnackbarType
)