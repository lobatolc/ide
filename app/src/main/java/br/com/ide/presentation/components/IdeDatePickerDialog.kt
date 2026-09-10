package br.com.ide.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.ide.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeDatePickerDialog(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
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

    val today =
        LocalDate.now()

    val todayMillis =
        today
            .atStartOfDay(
                ZoneOffset.UTC
            )
            .toInstant()
            .toEpochMilli()

    val selectableDates =
        remember(
            todayMillis,
            today.year
        ) {
            object : SelectableDates {

                override fun isSelectableDate(
                    utcTimeMillis: Long
                ): Boolean {
                    return utcTimeMillis >=
                            todayMillis
                }

                override fun isSelectableYear(
                    year: Int
                ): Boolean {
                    return year >=
                            today.year
                }
            }
        }

    val initialSelectedDateMillis =
        selectedDate
            ?.atStartOfDay(
                ZoneOffset.UTC
            )
            ?.toInstant()
            ?.toEpochMilli()

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialSelectedDateMillis,
            selectableDates =
                selectableDates
        )

    CompositionLocalProvider(
        LocalContext provides
                localizedContext
    ) {

        DatePickerDialog(
            onDismissRequest =
                onDismiss,

            confirmButton = {
                TextButton(
                    onClick = {

                        val selectedMillis =
                            datePickerState
                                .selectedDateMillis

                        if (
                            selectedMillis != null
                        ) {
                            val date =
                                Instant
                                    .ofEpochMilli(
                                        selectedMillis
                                    )
                                    .atZone(
                                        ZoneOffset.UTC
                                    )
                                    .toLocalDate()

                            onDateSelected(
                                date
                            )
                        }

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
            }
        ) {

            DatePicker(
                state =
                    datePickerState,
                showModeToggle = false,

                title = {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .common_select_date
                            ),
                        modifier =
                            Modifier
                                .padding(
                                    start = 24.dp,
                                    end = 24.dp,
                                    top = 16.dp
                                ),
                        style =
                            androidx.compose
                                .material3
                                .MaterialTheme
                                .typography
                                .labelLarge,
                        color =
                            androidx.compose
                                .material3
                                .MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                },

                headline = {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .common_date
                            ),
                        modifier =
                            Modifier
                                .padding(
                                    start = 24.dp,
                                    end = 24.dp,
                                    bottom = 12.dp
                                ),
                        style =
                            androidx.compose
                                .material3
                                .MaterialTheme
                                .typography
                                .headlineMedium,
                        color =
                            androidx.compose
                                .material3
                                .MaterialTheme
                                .colorScheme
                                .onSurface
                    )
                }
            )
        }
    }
}