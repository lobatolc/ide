package br.com.ide.presentation.components.snackbar

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdeSnackbarManager @Inject constructor() {

    private val messagesChannel =
        Channel<IdeSnackbarMessage>(
            capacity = Channel.BUFFERED
        )

    val messages =
        messagesChannel
            .receiveAsFlow()

    suspend fun show(
        message: IdeSnackbarMessage
    ) {
        messagesChannel.send(
            message
        )
    }
}