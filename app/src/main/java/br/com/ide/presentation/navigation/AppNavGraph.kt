package br.com.ide.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import br.com.ide.presentation.feature.home.HomeScreen
import br.com.ide.presentation.feature.login.LoginScreen
import br.com.ide.presentation.feature.mission.MissionScreen
import br.com.ide.presentation.feature.profile.ProfileScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.com.ide.presentation.feature.completeregistration.CompleteRegistrationScreen
import br.com.ide.presentation.feature.editprofile.EditProfileScreen
import br.com.ide.presentation.feature.forgotpassword.ForgotPasswordScreen
import br.com.ide.presentation.feature.forgotpassword.ForgotPasswordViewModel
import br.com.ide.presentation.feature.home.HomeViewModel
import br.com.ide.presentation.feature.login.LoginEvent
import br.com.ide.presentation.feature.login.LoginViewModel
import br.com.ide.presentation.feature.register.RegisterScreen
import br.com.ide.presentation.feature.register.RegisterViewModel
import br.com.ide.presentation.model.AppLanguage
import br.com.ide.presentation.session.SessionViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.toRoute
import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.presentation.feature.createmission.CreateMissionScreen
import br.com.ide.presentation.feature.profile.ProfileEvent
import br.com.ide.presentation.feature.profile.ProfileViewModel
import br.com.ide.presentation.feature.usermanagement.UserManagementEvent
import br.com.ide.presentation.feature.usermanagement.UserManagementScreen
import br.com.ide.presentation.feature.usermanagement.UserManagementViewModel
import br.com.ide.presentation.feature.usermanagementdetails.UserManagementDetailsScreen
import br.com.ide.presentation.model.AppTheme

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
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable<Login> {

            val viewModel: LoginViewModel = hiltViewModel()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(
                uiState.isLoggedIn
            ) {
                if (uiState.isLoggedIn) {

                    navController.navigate(Home) {
                        popUpTo<Login> {
                            inclusive = true
                        }
                    }
                }
            }

            LaunchedEffect(
                uiState.googleUserToComplete
            ) {
                if (uiState.googleUserToComplete != null) {

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
                uiState = uiState,
                onEvent = viewModel::onEvent,

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

        composable<Register> {
            val viewModel: RegisterViewModel = hiltViewModel()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isRegistered) {
                if (uiState.isRegistered) {
                    navController.navigate(Home) {
                        popUpTo<Login> {
                            inclusive = true
                        }
                    }
                }
            }

            RegisterScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<CompleteRegistration> {

            CompleteRegistrationScreen(
                onRegistrationCompleted = {

                    onSessionChanged()

                    navController.navigate(Home) {
                        popUpTo<Login> {
                            inclusive = true
                        }
                    }
                },

                onCancelRegistration = {

                    onLogout()

                    navController.navigate(Login) {
                        popUpTo<Login> {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<ForgotPassword> {
            val viewModel: ForgotPasswordViewModel = hiltViewModel()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ForgotPasswordScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

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

        composable<Mission> {
            MissionScreen()
        }

        composable<CreateMission> {

            CreateMissionScreen(
                onBackClick = {
                    navController.popBackStack()
                },

                onNextClick = {
                    // Próxima etapa:
                    // Participantes
                }
            )
        }

        composable<Profile> { backStackEntry ->

            val viewModel: ProfileViewModel =
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
                if (profileUpdated) {

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
                                        .valueOf(it)
                                }.getOrNull()
                            }

                    if (
                        firstName.isNotBlank() &&
                        lastName.isNotBlank() &&
                        sabbathSchoolClass != null
                    ) {
                        viewModel.onEvent(
                            ProfileEvent.ProfileUpdated(
                                firstName = firstName,
                                lastName = lastName,
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
                appLanguage = appLanguage,
                onLanguageChanged = onLanguageChanged,

                appTheme = appTheme,
                onThemeChanged = onThemeChanged,

                onHomeClick = {
                    navController.navigate(Home) {
                        launchSingleTop = true
                    }
                },

                onMetricsClick = {
                },

                onEditProfileClick = {
                    navController.navigate(EditProfile)
                },

                onNotificationsClick = {
                },

                onLogout = {
                    onLogout()

                    navController.navigate(Login) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },

                onUserManagementClick = {
                    navController.navigate(
                        UserManagement
                    )
                },

                viewModel = viewModel
            )
        }

        composable<UserManagement> { backStackEntry ->

            val viewModel: UserManagementViewModel =
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
                if (userManagementUpdated) {

                    viewModel.onEvent(
                        UserManagementEvent.Refresh
                    )

                    backStackEntry
                        .savedStateHandle[
                        "user_management_updated"
                    ] = false
                }
            }

            UserManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                },

                onUserClick = { userId ->
                    navController.navigate(
                        UserManagementDetails(
                            userId = userId
                        )
                    )
                },

                viewModel = viewModel
            )
        }

        composable<UserManagementDetails> {
                backStackEntry ->

            val route =
                backStackEntry
                    .toRoute<UserManagementDetails>()

            UserManagementDetailsScreen(
                userId =
                    route.userId,

                onBackClick = {
                    navController.popBackStack()
                },

                onSaved = {

                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            "user_management_updated",
                            true
                        )

                    navController.popBackStack()
                }
            )
        }

        composable<EditProfile> {

            EditProfileScreen(
                onBackClick = {
                    navController.popBackStack()
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
                                sabbathSchoolClass.name
                            )

                            set(
                                "profile_updated",
                                true
                            )
                        }

                    navController.popBackStack()
                }
            )
        }
    }
}