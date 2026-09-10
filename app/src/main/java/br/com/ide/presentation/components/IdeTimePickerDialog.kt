package br.com.ide.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.ide.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeTimePickerDialog(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val context =
        LocalContext.current

    val configuration =
        LocalConfiguration.current

    val localizedContext =
        remember(
            context,
            configuration
        ) {
            val localizedConfiguration =
                Configuration(
                    configuration
                )

            context.createConfigurationContext(
                localizedConfiguration
            )
        }

    val initialTime =
        selectedTime
            ?: LocalTime.now()

    val timePickerState =
        rememberTimePickerState(
            initialHour =
                initialTime.hour,
            initialMinute =
                initialTime.minute,
            is24Hour =
                true
        )

    CompositionLocalProvider(
        LocalContext provides
                localizedContext
    ) {

        AlertDialog(
            onDismissRequest =
                onDismiss,

            confirmButton = {
                TextButton(
                    onClick = {

                        onTimeSelected(
                            LocalTime.of(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        )

                        onDismiss()
                    }
                ) {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .common_confirm
                            )
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick =
                        onDismiss
                ) {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .common_cancel
                            )
                    )
                }
            },

            text = {
                Column(
                    modifier =
                        Modifier.padding(
                            top = 8.dp
                        )
                ) {

                    TimePicker(
                        state =
                            timePickerState
                    )
                }
            }
        )
    }
}