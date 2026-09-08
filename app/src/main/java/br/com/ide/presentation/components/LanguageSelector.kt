package br.com.ide.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.com.ide.R
import br.com.ide.presentation.model.AppLanguage

@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
    ) {

        IconButton(
            onClick = {
                expanded = true
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = stringResource(
                    R.string.language_selector_description
                )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            R.string.language_portuguese
                        )
                    )
                },
                trailingIcon = {
                    if (
                        selectedLanguage ==
                        AppLanguage.PORTUGUESE
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                },
                onClick = {
                    onLanguageSelected(
                        AppLanguage.PORTUGUESE
                    )

                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            R.string.language_english
                        )
                    )
                },
                trailingIcon = {
                    if (
                        selectedLanguage ==
                        AppLanguage.ENGLISH
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                },
                onClick = {
                    onLanguageSelected(
                        AppLanguage.ENGLISH
                    )

                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            R.string.language_spanish
                        )
                    )
                },
                trailingIcon = {
                    if (
                        selectedLanguage ==
                        AppLanguage.SPANISH
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                },
                onClick = {
                    onLanguageSelected(
                        AppLanguage.SPANISH
                    )

                    expanded = false
                }
            )
        }
    }
}