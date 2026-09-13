package br.com.ide.presentation.util

import android.content.Context
import android.content.res.Configuration
import br.com.ide.presentation.model.AppLanguage
import java.util.Locale

fun Context.forLanguage(
    language: AppLanguage
): Context {

    val languageTag =
        when (language) {

            AppLanguage.PORTUGUESE ->
                "pt-BR"

            AppLanguage.ENGLISH ->
                "en"

            AppLanguage.SPANISH ->
                "es"
        }

    val locale =
        Locale.forLanguageTag(
            languageTag
        )

    val configuration =
        Configuration(
            resources.configuration
        )

    configuration.setLocale(
        locale
    )

    return createConfigurationContext(
        configuration
    )
}