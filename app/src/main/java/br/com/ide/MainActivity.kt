package br.com.ide

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import br.com.ide.data.location.MissionLocationService
import br.com.ide.presentation.locale.ProvideLocalizedContext
import br.com.ide.presentation.navigation.AppNavGraph
import br.com.ide.presentation.navigation.CompleteRegistration
import br.com.ide.presentation.navigation.Home
import br.com.ide.presentation.navigation.Login
import br.com.ide.presentation.session.SessionState
import br.com.ide.presentation.session.SessionViewModel
import br.com.ide.presentation.settings.AppSettingsViewModel
import br.com.ide.presentation.theme.IdeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val supportNotificationMissionId =
        mutableStateOf<String?>(
            null
        )

    private val supportNotificationUserId =
        mutableStateOf<String?>(
            null
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        readSupportNotificationIntent(
            intent
        )

        setContent {

            val settingsViewModel:
                    AppSettingsViewModel =
                hiltViewModel()

            val sessionViewModel:
                    SessionViewModel =
                hiltViewModel()

            val language by
            settingsViewModel.language
                .collectAsStateWithLifecycle()

            val theme by
            settingsViewModel.theme
                .collectAsStateWithLifecycle()

            val sessionState by
            sessionViewModel.sessionState
                .collectAsStateWithLifecycle()

            val pendingSupportMissionId by
            supportNotificationMissionId

            val pendingSupportUserId by
            supportNotificationUserId

            ProvideLocalizedContext(
                language = language
            ) {

                IdeTheme(
                    appTheme = theme
                ) {

                    val navController =
                        rememberNavController()

                    when (sessionState) {

                        SessionState.Loading -> {

                            Box(
                                modifier =
                                    Modifier.fillMaxSize(),
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                CircularProgressIndicator(
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )
                            }
                        }

                        SessionState.LoggedOut -> {

                            AppNavGraph(
                                navController =
                                    navController,

                                appLanguage =
                                    language,

                                onLanguageChanged =
                                    settingsViewModel::changeLanguage,

                                appTheme =
                                    theme,

                                onThemeChanged =
                                    settingsViewModel::changeTheme,

                                startDestination =
                                    Login,

                                onSessionChanged =
                                    sessionViewModel::checkSession,

                                onLogout =
                                    sessionViewModel::logout
                            )
                        }

                        SessionState.NeedsRegistration -> {

                            AppNavGraph(
                                navController =
                                    navController,

                                appLanguage =
                                    language,

                                onLanguageChanged =
                                    settingsViewModel::changeLanguage,

                                appTheme =
                                    theme,

                                onThemeChanged =
                                    settingsViewModel::changeTheme,

                                startDestination =
                                    CompleteRegistration,

                                onSessionChanged =
                                    sessionViewModel::checkSession,

                                onLogout =
                                    sessionViewModel::logout
                            )
                        }

                        SessionState.LoggedIn -> {

                            AppNavGraph(
                                navController =
                                    navController,

                                appLanguage =
                                    language,

                                onLanguageChanged =
                                    settingsViewModel::changeLanguage,

                                appTheme =
                                    theme,

                                onThemeChanged =
                                    settingsViewModel::changeTheme,

                                startDestination =
                                    Home,

                                onSessionChanged =
                                    sessionViewModel::checkSession,

                                onLogout =
                                    sessionViewModel::logout,

                                supportNotificationMissionId =
                                    pendingSupportMissionId,

                                supportNotificationUserId =
                                    pendingSupportUserId,

                                onSupportNotificationHandled = {
                                    clearSupportNotification()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(
            intent
        )

        readSupportNotificationIntent(
            intent
        )
    }

    private fun readSupportNotificationIntent(
        intent: Intent?
    ) {

        val missionId =
            intent
                ?.getStringExtra(
                    MissionLocationService
                        .EXTRA_SUPPORT_MISSION_ID
                )
                ?.takeIf {
                    it.isNotBlank()
                }

        val userId =
            intent
                ?.getStringExtra(
                    MissionLocationService
                        .EXTRA_SUPPORT_USER_ID
                )
                ?.takeIf {
                    it.isNotBlank()
                }

        if (
            missionId != null &&
            userId != null
        ) {

            supportNotificationMissionId.value =
                missionId

            supportNotificationUserId.value =
                userId
        }
    }

    private fun clearSupportNotification() {

        supportNotificationMissionId.value =
            null

        supportNotificationUserId.value =
            null
    }
}
