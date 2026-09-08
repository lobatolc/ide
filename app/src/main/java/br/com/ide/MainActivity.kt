package br.com.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import br.com.ide.presentation.locale.ProvideLocalizedContext
import br.com.ide.presentation.navigation.AppNavGraph
import br.com.ide.presentation.navigation.Home
import br.com.ide.presentation.navigation.Login
import br.com.ide.presentation.session.SessionViewModel
import br.com.ide.presentation.settings.AppSettingsViewModel
import br.com.ide.presentation.theme.IdeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            val settingsViewModel:
                    AppSettingsViewModel = hiltViewModel()

            val sessionViewModel:
                    SessionViewModel = hiltViewModel()

            val language =
                settingsViewModel.language
                    .collectAsStateWithLifecycle()

            val startDestination =
                if (sessionViewModel.isUserLoggedIn()) {
                    Home
                } else {
                    Login
                }

            ProvideLocalizedContext(
                language = language.value
            ) {
                IdeTheme {

                    val navController =
                        rememberNavController()

                    AppNavGraph(
                        navController = navController,
                        appLanguage = language.value,
                        onLanguageChanged =
                            settingsViewModel::changeLanguage,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}