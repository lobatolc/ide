package br.com.ide.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.presentation.components.snackbar.IdeSnackbarHost
import br.com.ide.presentation.components.snackbar.IdeSnackbarVisuals
import br.com.ide.presentation.feature.completeregistration.CompleteRegistrationScreen
import br.com.ide.presentation.feature.createmission.CreateMissionScreen
import br.com.ide.presentation.feature.editprofile.EditProfileScreen
import br.com.ide.presentation.feature.forgotpassword.ForgotPasswordScreen
import br.com.ide.presentation.feature.forgotpassword.ForgotPasswordViewModel
import br.com.ide.presentation.feature.home.HomeScreen
import br.com.ide.presentation.feature.login.LoginEvent
import br.com.ide.presentation.feature.login.LoginScreen
import br.com.ide.presentation.feature.login.LoginViewModel
import br.com.ide.presentation.feature.mission.MissionScreen
import br.com.ide.presentation.feature.missionplanning.MissionPlanningScreen
import br.com.ide.presentation.feature.profile.ProfileEvent
import br.com.ide.presentation.feature.profile.ProfileScreen
import br.com.ide.presentation.feature.profile.ProfileViewModel
import br.com.ide.presentation.feature.register.RegisterScreen
import br.com.ide.presentation.feature.register.RegisterViewModel
import br.com.ide.presentation.feature.usermanagement.UserManagementEvent
import br.com.ide.presentation.feature.usermanagement.UserManagementScreen
import br.com.ide.presentation.feature.usermanagement.UserManagementViewModel
import br.com.ide.presentation.feature.usermanagementdetails.UserManagementDetailsScreen
import br.com.ide.presentation.model.AppLanguage
import br.com.ide.presentation.model.AppTheme
import br.com.ide.presentation.snackbar.AppSnackbarViewModel
import br.com.ide.presentation.util.forLanguage

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    appTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    startDestination: Any,
    onSessionChanged: () -> Unit,
    onLogout: () -> Unit
) {

    val snackbarViewModel:
            AppSnackbarViewModel =
        hiltViewModel()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    val context =
        LocalContext.current

    // =========================================================
    // Snackbar global
    // =========================================================

    LaunchedEffect(
        snackbarViewModel,
        appLanguage
    ) {

        snackbarViewModel
            .messages
            .collect { message ->

                val localizedContext =
                    context.forLanguage(
                        appLanguage
                    )

                snackbarHostState.showSnackbar(
                    visuals =
                        IdeSnackbarVisuals(
                            message =
                                localizedContext
                                    .getString(
                                        message.messageRes
                                    ),
                            type =
                                message.type
                        )
                )
            }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
    ) {

        NavHost(
            navController =
                navController,
            startDestination =
                startDestination
        ) {

            // =====================================================
            // Login
            // =====================================================

            composable<Login> {

                val viewModel:
                        LoginViewModel =
                    hiltViewModel()

                val uiState by
                viewModel
                    .uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(
                    uiState.isLoggedIn
                ) {

                    if (
                        uiState.isLoggedIn
                    ) {

                        navController.navigate(
                            Home
                        ) {

                            popUpTo<Login> {
                                inclusive =
                                    true
                            }
                        }
                    }
                }

                LaunchedEffect(
                    uiState.googleUserToComplete
                ) {

                    if (
                        uiState.googleUserToComplete !=
                        null
                    ) {

                        navController.navigate(
                            CompleteRegistration
                        )

                        viewModel.onEvent(
                            LoginEvent
                                .CompleteRegistrationNavigationHandled
                        )
                    }
                }

                LoginScreen(
                    uiState =
                        uiState,

                    onEvent =
                        viewModel::onEvent,

                    onForgotPasswordClick = {

                        navController.navigate(
                            ForgotPassword
                        )
                    },

                    onCreateAccountClick = {

                        navController.navigate(
                            Register
                        )
                    }
                )
            }

            // =====================================================
            // Cadastro
            // =====================================================

            composable<Register> {

                val viewModel:
                        RegisterViewModel =
                    hiltViewModel()

                val uiState by
                viewModel
                    .uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(
                    uiState.isRegistered
                ) {

                    if (
                        uiState.isRegistered
                    ) {

                        navController.navigate(
                            Home
                        ) {

                            popUpTo<Login> {
                                inclusive =
                                    true
                            }
                        }
                    }
                }

                RegisterScreen(
                    uiState =
                        uiState,

                    onEvent =
                        viewModel::onEvent,

                    onBackClick = {
                        navController
                            .popBackStack()
                    },

                    onLoginClick = {
                        navController
                            .popBackStack()
                    }
                )
            }

            // =====================================================
            // Completar cadastro
            // =====================================================

            composable<CompleteRegistration> {

                CompleteRegistrationScreen(

                    onRegistrationCompleted = {

                        onSessionChanged()

                        navController.navigate(
                            Home
                        ) {

                            popUpTo<Login> {
                                inclusive =
                                    true
                            }
                        }
                    },

                    onCancelRegistration = {

                        onLogout()

                        navController.navigate(
                            Login
                        ) {

                            popUpTo<Login> {
                                inclusive =
                                    true
                            }
                        }
                    }
                )
            }

            // =====================================================
            // Recuperar senha
            // =====================================================

            composable<ForgotPassword> {

                val viewModel:
                        ForgotPasswordViewModel =
                    hiltViewModel()

                val uiState by
                viewModel
                    .uiState
                    .collectAsStateWithLifecycle()

                ForgotPasswordScreen(
                    uiState =
                        uiState,

                    onEvent =
                        viewModel::onEvent,

                    onBackClick = {
                        navController
                            .popBackStack()
                    },

                    onLoginClick = {
                        navController
                            .popBackStack()
                    }
                )
            }

            // =====================================================
            // Home
            // =====================================================

            composable<Home> {

                HomeScreen(

                    onMissionClick = { _ ->

                        navController.navigate(
                            Mission
                        )
                    },

                    onMetricsClick = {
                    },

                    onProfileClick = {

                        navController.navigate(
                            Profile
                        )
                    },

                    onFilterClick = {
                    },

                    onCreateMissionClick = {

                        navController.navigate(
                            CreateMission
                        )
                    }
                )
            }

            // =====================================================
            // Missão
            // =====================================================

            composable<Mission> {

                MissionScreen()
            }

            // =====================================================
            // Criar missão
            // =====================================================

            composable<CreateMission> {

                CreateMissionScreen(

                    onBackClick = {

                        navController
                            .popBackStack()
                    },

                    onMissionCreated = { missionId ->

                        navController.navigate(
                            MissionPlanning(
                                missionId =
                                    missionId
                            )
                        ) {

                            /*
                             * Remove completamente
                             * CreateMission da pilha.
                             *
                             * Assim o usuário nunca
                             * volta para o resumo
                             * depois da criação.
                             */
                            popUpTo<CreateMission> {
                                inclusive =
                                    true
                            }
                        }
                    }
                )
            }

            // =====================================================
            // Planejamento da missão
            // =====================================================

            composable<MissionPlanning> {
                    backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<MissionPlanning>()

                MissionPlanningScreen(
                    missionId =
                        route.missionId,

                    onBackClick = {

                        /*
                         * CreateMission já foi removida
                         * da pilha.
                         *
                         * Portanto essa seta volta
                         * naturalmente para a Home.
                         */
                        navController
                            .popBackStack()
                    }
                )
            }

            // =====================================================
            // Perfil
            // =====================================================

            composable<Profile> {
                    backStackEntry ->

                val viewModel:
                        ProfileViewModel =
                    hiltViewModel()

                val profileUpdated by
                backStackEntry
                    .savedStateHandle
                    .getStateFlow(
                        "profile_updated",
                        false
                    )
                    .collectAsStateWithLifecycle()

                LaunchedEffect(
                    profileUpdated
                ) {

                    if (
                        profileUpdated
                    ) {

                        val firstName =
                            backStackEntry
                                .savedStateHandle
                                .get<String>(
                                    "profile_first_name"
                                )
                                .orEmpty()

                        val lastName =
                            backStackEntry
                                .savedStateHandle
                                .get<String>(
                                    "profile_last_name"
                                )
                                .orEmpty()

                        val sabbathSchoolClassName =
                            backStackEntry
                                .savedStateHandle
                                .get<String>(
                                    "profile_sabbath_class"
                                )

                        val sabbathSchoolClass =
                            sabbathSchoolClassName
                                ?.let {

                                    runCatching {

                                        SabbathSchoolClass
                                            .valueOf(
                                                it
                                            )

                                    }.getOrNull()
                                }

                        if (
                            firstName.isNotBlank() &&
                            lastName.isNotBlank() &&
                            sabbathSchoolClass !=
                            null
                        ) {

                            viewModel.onEvent(
                                ProfileEvent
                                    .ProfileUpdated(
                                        firstName =
                                            firstName,
                                        lastName =
                                            lastName,
                                        sabbathSchoolClass =
                                            sabbathSchoolClass
                                    )
                            )
                        }

                        backStackEntry
                            .savedStateHandle[
                            "profile_updated"
                        ] = false
                    }
                }

                ProfileScreen(
                    appLanguage =
                        appLanguage,

                    onLanguageChanged =
                        onLanguageChanged,

                    appTheme =
                        appTheme,

                    onThemeChanged =
                        onThemeChanged,

                    onHomeClick = {

                        navController.navigate(
                            Home
                        ) {

                            launchSingleTop =
                                true
                        }
                    },

                    onMetricsClick = {
                    },

                    onEditProfileClick = {

                        navController.navigate(
                            EditProfile
                        )
                    },

                    onNotificationsClick = {
                    },

                    onLogout = {

                        onLogout()

                        navController.navigate(
                            Login
                        ) {

                            popUpTo(
                                0
                            ) {
                                inclusive =
                                    true
                            }
                        }
                    },

                    onUserManagementClick = {

                        navController.navigate(
                            UserManagement
                        )
                    },

                    viewModel =
                        viewModel
                )
            }

            // =====================================================
            // Gerenciamento de usuários
            // =====================================================

            composable<UserManagement> {
                    backStackEntry ->

                val viewModel:
                        UserManagementViewModel =
                    hiltViewModel()

                val userManagementUpdated by
                backStackEntry
                    .savedStateHandle
                    .getStateFlow(
                        "user_management_updated",
                        false
                    )
                    .collectAsStateWithLifecycle()

                LaunchedEffect(
                    userManagementUpdated
                ) {

                    if (
                        userManagementUpdated
                    ) {

                        viewModel.onEvent(
                            UserManagementEvent
                                .Refresh
                        )

                        backStackEntry
                            .savedStateHandle[
                            "user_management_updated"
                        ] = false
                    }
                }

                UserManagementScreen(

                    onBackClick = {

                        navController
                            .popBackStack()
                    },

                    onUserClick = { userId ->

                        navController.navigate(
                            UserManagementDetails(
                                userId =
                                    userId
                            )
                        )
                    },

                    viewModel =
                        viewModel
                )
            }

            // =====================================================
            // Detalhes / edição de usuário
            // =====================================================

            composable<UserManagementDetails> {
                    backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<UserManagementDetails>()

                UserManagementDetailsScreen(
                    userId =
                        route.userId,

                    onBackClick = {

                        navController
                            .popBackStack()
                    },

                    onSaved = {

                        navController
                            .previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "user_management_updated",
                                true
                            )

                        navController
                            .popBackStack()
                    }
                )
            }

            // =====================================================
            // Editar perfil
            // =====================================================

            composable<EditProfile> {

                EditProfileScreen(

                    onBackClick = {

                        navController
                            .popBackStack()
                    },

                    onSaved = {
                            firstName,
                            lastName,
                            sabbathSchoolClass ->

                        navController
                            .previousBackStackEntry
                            ?.savedStateHandle
                            ?.apply {

                                set(
                                    "profile_first_name",
                                    firstName
                                )

                                set(
                                    "profile_last_name",
                                    lastName
                                )

                                set(
                                    "profile_sabbath_class",
                                    sabbathSchoolClass
                                        .name
                                )

                                set(
                                    "profile_updated",
                                    true
                                )
                            }

                        navController
                            .popBackStack()
                    }
                )
            }
        }

        // =========================================================
        // Snackbar global
        // =========================================================

        IdeSnackbarHost(
            hostState =
                snackbarHostState,
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(
                        16.dp
                    )
        )
    }
}