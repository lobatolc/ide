package br.com.ide.presentation.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import br.com.ide.presentation.model.AppLanguage
import java.util.Locale

@Composable
fun ProvideLocalizedContext(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentConfiguration =
        LocalConfiguration.current

    val localizedConfiguration = remember(
        currentConfiguration,
        language
    ) {
        Configuration(
            currentConfiguration
        ).apply {
            setLocale(
                Locale.forLanguageTag(
                    language.languageTag
                )
            )
        }
    }

    val localizedResources = remember(
        context,
        localizedConfiguration
    ) {
        context
            .createConfigurationContext(
                localizedConfiguration
            )
            .resources
    }

    CompositionLocalProvider(
        LocalConfiguration provides
                localizedConfiguration,

        LocalResources provides
                localizedResources
    ) {
        content()
    }
}