package br.com.ide.presentation.components.snackbar

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppSnackbarViewModel @Inject constructor(
    snackbarManager:
    IdeSnackbarManager
) : ViewModel() {

    val messages =
        snackbarManager.messages
}