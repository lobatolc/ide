package br.com.ide.presentation.snackbar

import androidx.lifecycle.ViewModel
import br.com.ide.presentation.components.snackbar.IdeSnackbarManager
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