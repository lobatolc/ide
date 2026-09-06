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
import br.com.ide.presentation.feature.home.HomeViewModel
import br.com.ide.presentation.feature.login.LoginViewModel
import br.com.ide.presentation.feature.register.RegisterScreen
import br.com.ide.presentation.feature.register.RegisterViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Login
    ) {

        composable<Login> {

            val viewModel: LoginViewModel =
                hiltViewModel()

            val uiState by
            viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.isLoggedIn) {

                if (uiState.isLoggedIn) {

                    navController.navigate(Home) {

                        popUpTo<Login> {
                            inclusive = true
                        }
                    }
                }
            }

            LoginScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onCreateAccountClick = {
                    navController.navigate(Register)
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

        composable<Home> {

            val viewModel: HomeViewModel = hiltViewModel()

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            HomeScreen(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onMissionClick = {
                    navController.navigate(Mission)
                }
            )
        }

        composable<Mission> {
            MissionScreen()
        }

        composable<Profile> {
            ProfileScreen()
        }
    }
}