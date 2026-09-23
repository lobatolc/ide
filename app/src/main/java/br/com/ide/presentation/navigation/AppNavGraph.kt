package br.com.ide.presentation.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import br.com.ide.R
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.presentation.components.snackbar.IdeSnackbarHost
import br.com.ide.presentation.components.snackbar.IdeSnackbarVisuals
import br.com.ide.presentation.components.snackbar.IdeSnackbarType
import br.com.ide.presentation.feature.completeregistration.CompleteRegistrationScreen
import br.com.ide.presentation.feature.createmission.CreateMissionScreen
import br.com.ide.presentation.feature.editprofile.EditProfileScreen
import br.com.ide.presentation.feature.forgotpassword.ForgotPasswordScreen
import br.com.ide.presentation.feature.forgotpassword.ForgotPasswordViewModel
import br.com.ide.presentation.feature.home.HomeScreen
import br.com.ide.presentation.feature.metrics.MetricsScreen
import br.com.ide.presentation.feature.login.LoginEvent
import br.com.ide.presentation.feature.login.LoginScreen
import br.com.ide.presentation.feature.login.LoginViewModel
import br.com.ide.presentation.feature.mission.MissionScreen
import br.com.ide.presentation.feature.missionarea.MissionAreaScreen
import br.com.ide.presentation.feature.missionexecution.MissionExecutionScreen
import br.com.ide.presentation.feature.missionexecution.MissionExecutionViewModel
import br.com.ide.presentation.feature.missionmetrics.MissionGeneralMetricsLabels
import br.com.ide.presentation.feature.missionmetrics.MissionGeneralMetricsScreen
import br.com.ide.presentation.feature.missionmetrics.MissionPersonalMetricsLabels
import br.com.ide.presentation.feature.missionmetrics.MissionPersonalMetricsScreen
import br.com.ide.presentation.feature.missiongroups.MissionGroupsScreen
import br.com.ide.presentation.feature.missionlocations.MissionLocationsScreen
import br.com.ide.presentation.feature.missionplanning.MissionPlanningScreen
import br.com.ide.presentation.feature.newencounter.NewEncounterScreen
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
import br.com.ide.presentation.components.snackbar.AppSnackbarViewModel
import br.com.ide.presentation.util.forLanguage
import androidx.compose.foundation.layout.navigationBarsPadding
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    appTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    startDestination: Any,
    onSessionChanged: () -> Unit,
    onLogout: () -> Unit,
    supportNotificationMissionId: String? = null,
    supportNotificationUserId: String? = null,
    onSupportNotificationHandled: () -> Unit = {}
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

    val coroutineScope =
        rememberCoroutineScope()

    var supportFocusMissionId by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    var supportFocusUserId by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    LaunchedEffect(
        supportNotificationMissionId,
        supportNotificationUserId
    ) {

        val missionId =
            supportNotificationMissionId
                ?.takeIf {
                    it.isNotBlank()
                }

        val userId =
            supportNotificationUserId
                ?.takeIf {
                    it.isNotBlank()
                }

        if (
            missionId != null &&
            userId != null
        ) {

            supportFocusMissionId =
                missionId

            supportFocusUserId =
                userId

            navController.navigate(
                MissionExecution(
                    missionId =
                        missionId
                )
            ) {
                launchSingleTop =
                    true
            }

            onSupportNotificationHandled()
        }
    }

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
                startDestination,
            modifier =
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
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

                    onMissionClick = {
                            missionId,
                            missionStatus ->

                        when (
                            missionStatus
                        ) {

                            MissionStatus.IN_PROGRESS -> {

                                navController.navigate(
                                    MissionExecution(
                                        missionId =
                                            missionId
                                    )
                                )
                            }

                            MissionStatus.PLANNING,
                            MissionStatus.SCHEDULED,
                            MissionStatus.CANCELLED -> {

                                navController.navigate(
                                    MissionPlanning(
                                        missionId =
                                            missionId
                                    )
                                )
                            }

                            MissionStatus.COMPLETED -> {
                                /*
                                 * A Home já intercepta este status e
                                 * exibe uma snackbar de aviso.
                                 *
                                 * Mantemos a proteção também aqui para
                                 * impedir que uma missão concluída volte
                                 * ao planejamento caso algum fluxo futuro
                                 * dispare esta callback diretamente.
                                 */
                                Unit
                            }
                        }
                    },

                    onMetricsClick = {

                        navController.navigate(
                            Metrics
                        ) {
                            launchSingleTop = true
                        }
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
            // Métricas históricas
            // =====================================================

            composable<Metrics> {

                MetricsScreen(
                    onHomeClick = {

                        navController.navigate(
                            Home
                        ) {
                            launchSingleTop = true

                            popUpTo<Home> {
                                inclusive = false
                            }
                        }
                    },

                    onProfileClick = {

                        navController.navigate(
                            Profile
                        ) {
                            launchSingleTop = true
                        }
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

            composable<MissionPlanning> { backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<MissionPlanning>()

                MissionPlanningScreen(
                    missionId =
                        route.missionId,

                    onBackClick = {
                        navController.popBackStack()
                    },

                    onLocationsClick = {

                        navController.navigate(
                            MissionLocations(
                                missionId =
                                    route.missionId
                            )
                        )
                    },

                    onGroupsClick = {

                        navController.navigate(
                            MissionGroups(
                                missionId =
                                    route.missionId
                            )
                        )
                    },

                    onAreaClick = {
                        navController.navigate(
                            MissionArea(
                                missionId =
                                    route.missionId
                            )
                        )
                    },

                    onMissionStarted = {

                        navController.navigate(
                            MissionExecution(
                                missionId =
                                    route.missionId
                            )
                        ) {

                            popUpTo<MissionPlanning> {
                                inclusive =
                                    true
                            }
                        }
                    }
                )
            }

            // =====================================================
            // Execução da missão
            // =====================================================

            composable<MissionExecution> { backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<MissionExecution>()

                MissionExecutionScreen(
                    missionId =
                        route.missionId,

                    focusParticipantUserId =
                        supportFocusUserId
                            ?.takeIf {
                                supportFocusMissionId ==
                                        route.missionId
                            },

                    onFocusParticipantHandled = {

                        supportFocusMissionId =
                            null

                        supportFocusUserId =
                            null
                    },

                    onBackClick = {
                        navController
                            .popBackStack()
                    },

                    onRegisterEncounterClick = {

                        navController.navigate(
                            NewEncounter(
                                missionId =
                                    route.missionId
                            )
                        )
                    },

                    onMetricsClick = {

                        navController.navigate(
                            MissionPersonalMetrics(
                                missionId =
                                    route.missionId
                            )
                        )
                    },

                    onGeneralMetricsClick = {

                        navController.navigate(
                            MissionGeneralMetrics(
                                missionId =
                                    route.missionId
                            )
                        )
                    },

                    onGroupsClick = {

                        navController.navigate(
                            MissionGroups(
                                missionId =
                                    route.missionId
                            )
                        )
                    },

                    onFinishMissionClick = {
                        navController.popBackStack()
                    },

                    onEndParticipationClick = {
                        navController
                            .popBackStack()
                    }
                )
            }

            // =====================================================
            // Minhas métricas
            // =====================================================

            composable<MissionPersonalMetrics> {
                    backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<MissionPersonalMetrics>()

                /*
                 * Quando esta tela é aberta a partir da execução,
                 * reutilizamos o mesmo MissionExecutionViewModel.
                 *
                 * Assim encontros, distância e tempo continuam
                 * atualizando sem criar uma segunda fonte de estado.
                 */
                val executionBackStackEntry =
                    navController
                        .previousBackStackEntry

                val viewModel:
                        MissionExecutionViewModel =
                    if (
                        executionBackStackEntry !=
                        null
                    ) {
                        hiltViewModel(
                            executionBackStackEntry
                        )
                    } else {
                        hiltViewModel()
                    }

                val uiState by
                viewModel
                    .uiState
                    .collectAsStateWithLifecycle()

                /*
                 * Em uma abertura normal o ViewModel já está
                 * carregado. A chamada também torna a rota segura
                 * caso ela seja aberta sem a tela de execução
                 * imediatamente anterior na pilha.
                 */
                LaunchedEffect(
                    route.missionId
                ) {
                    viewModel.load(
                        route.missionId
                    )
                }

                val customActivityLabel =
                    uiState
                        .customActivityName
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: if (
                            uiState
                                .personalMetrics
                                .customActivityCount >
                            0
                        ) {
                            stringResource(
                                R.string
                                    .mission_personal_metrics_custom_activity_fallback
                            )
                        } else {
                            null
                        }

                val labels =
                    MissionPersonalMetricsLabels(
                        title =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_title
                            ),
                        backContentDescription =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_back
                            ),
                        encounters =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_encounters
                            ),
                        visits =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_visits
                            ),
                        prayers =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_prayers
                            ),
                        customActivity =
                            customActivityLabel,
                        bibleStudiesOffered =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_bible_studies_offered
                            ),
                        bibleStudiesAccepted =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_bible_studies_accepted
                            ),
                        materialsDelivered =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_materials_delivered
                            ),
                        surveysCompleted =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_surveys_completed
                            ),
                        distance =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_distance
                            ),
                        participationTime =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_participation_time
                            ),
                        meterUnit =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_unit_meter
                            ),
                        kilometerUnit =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_unit_kilometer
                            ),
                        hourUnit =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_unit_hour
                            ),
                        minuteUnit =
                            stringResource(
                                R.string
                                    .mission_personal_metrics_unit_minute
                            )
                    )

                MissionPersonalMetricsScreen(
                    missionName =
                        uiState.missionName,
                    metrics =
                        uiState.personalMetrics,
                    labels =
                        labels,
                    onBackClick = {
                        navController
                            .popBackStack()
                    }
                )
            }

            // =====================================================
            // Métricas gerais
            // =====================================================

            composable<MissionGeneralMetrics> {
                    backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<MissionGeneralMetrics>()

                /*
                 * A tela é aberta a partir da execução da missão.
                 * Reutilizamos o mesmo MissionExecutionViewModel
                 * para que os totais continuem atualizando em tempo
                 * real enquanto esta tela estiver aberta.
                 */
                val executionBackStackEntry =
                    navController
                        .previousBackStackEntry

                val viewModel:
                        MissionExecutionViewModel =
                    if (
                        executionBackStackEntry !=
                        null
                    ) {
                        hiltViewModel(
                            executionBackStackEntry
                        )
                    } else {
                        hiltViewModel()
                    }

                val uiState by
                viewModel
                    .uiState
                    .collectAsStateWithLifecycle()

                /*
                 * Normalmente o ViewModel já vem carregado da tela de
                 * execução. Esta chamada também protege a navegação caso
                 * a rota seja aberta em outro cenário.
                 */
                LaunchedEffect(
                    route.missionId
                ) {
                    viewModel.load(
                        route.missionId
                    )
                }

                /*
                 * A opção não aparece para missionários, mas a rota
                 * também se protege contra uma abertura indevida.
                 * Somente líder, pastor e administrador permanecem.
                 */
                LaunchedEffect(
                    uiState.missionId,
                    uiState.canViewGeneralMetrics,
                    uiState.errorMessage
                ) {
                    if (
                        uiState.errorMessage !=
                        null ||
                        (
                                uiState.missionId ==
                                        route.missionId &&
                                        !uiState.canViewGeneralMetrics
                                )
                    ) {
                        navController
                            .popBackStack()
                    }
                }

                val canShowGeneralMetrics =
                    uiState.missionId ==
                            route.missionId &&
                            uiState.canViewGeneralMetrics

                if (
                    !canShowGeneralMetrics
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                    )

                    return@composable
                }

                val customActivityLabel =
                    uiState
                        .customActivityName
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: if (
                            br.com.ide.domain.model.MissionActivityType.OTHER in
                            uiState.availableActivities
                        ) {
                            stringResource(
                                R.string
                                    .mission_general_metrics_custom_activity_fallback
                            )
                        } else {
                            null
                        }

                val labels =
                    MissionGeneralMetricsLabels(
                        title =
                            stringResource(
                                R.string
                                    .mission_general_metrics_title
                            ),
                        backContentDescription =
                            stringResource(
                                R.string
                                    .mission_general_metrics_back
                            ),
                        participants =
                            stringResource(
                                R.string
                                    .mission_general_metrics_participants
                            ),
                        groups =
                            stringResource(
                                R.string
                                    .mission_general_metrics_groups
                            ),
                        encounters =
                            stringResource(
                                R.string
                                    .mission_general_metrics_encounters
                            ),
                        visits =
                            stringResource(
                                R.string
                                    .mission_general_metrics_visits
                            ),
                        prayers =
                            stringResource(
                                R.string
                                    .mission_general_metrics_prayers
                            ),
                        customActivity =
                            customActivityLabel,
                        bibleStudiesOffered =
                            stringResource(
                                R.string
                                    .mission_general_metrics_bible_studies_offered
                            ),
                        bibleStudiesAccepted =
                            stringResource(
                                R.string
                                    .mission_general_metrics_bible_studies_accepted
                            ),
                        materialsDelivered =
                            stringResource(
                                R.string
                                    .mission_general_metrics_materials_delivered
                            ),
                        surveysCompleted =
                            stringResource(
                                R.string
                                    .mission_general_metrics_surveys_completed
                            ),
                        distance =
                            stringResource(
                                R.string
                                    .mission_general_metrics_distance
                            ),
                        participationTime =
                            stringResource(
                                R.string
                                    .mission_general_metrics_participation_time
                            ),
                        meterUnit =
                            stringResource(
                                R.string
                                    .mission_general_metrics_unit_meter
                            ),
                        kilometerUnit =
                            stringResource(
                                R.string
                                    .mission_general_metrics_unit_kilometer
                            ),
                        hourUnit =
                            stringResource(
                                R.string
                                    .mission_general_metrics_unit_hour
                            ),
                        minuteUnit =
                            stringResource(
                                R.string
                                    .mission_general_metrics_unit_minute
                            )
                    )

                MissionGeneralMetricsScreen(
                    missionName =
                        uiState.missionName,
                    metrics =
                        uiState.generalMetrics,
                    availableActivities =
                        uiState.availableActivities,
                    labels =
                        labels,
                    onBackClick = {
                        navController
                            .popBackStack()
                    }
                )
            }

            // =====================================================
            // Novo encontro
            // =====================================================

            composable<NewEncounter> { backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<NewEncounter>()

                NewEncounterScreen(
                    missionId =
                        route.missionId,

                    onBackClick = {
                        navController
                            .popBackStack()
                    },

                    onEncounterSaved = { effect ->

                        val localizedContext =
                            context.forLanguage(
                                appLanguage
                            )

                        if (
                            effect.contactNow &&
                            effect.phoneDigits.length ==
                            11
                        ) {

                            val firstName =
                                effect.personName
                                    ?.trim()
                                    ?.substringBefore(" ")
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }

                            val message =
                                if (
                                    firstName != null
                                ) {
                                    localizedContext
                                        .getString(
                                            R.string
                                                .new_encounter_whatsapp_message_named,
                                            firstName
                                        )
                                } else {
                                    localizedContext
                                        .getString(
                                            R.string
                                                .new_encounter_whatsapp_message_unnamed
                                        )
                                }

                            val whatsappOpened =
                                openWhatsAppConversation(
                                    context =
                                        context,
                                    phoneDigits =
                                        effect.phoneDigits,
                                    message =
                                        message
                                )

                            if (
                                !whatsappOpened
                            ) {

                                coroutineScope.launch {

                                    snackbarHostState
                                        .showSnackbar(
                                            visuals =
                                                IdeSnackbarVisuals(
                                                    message =
                                                        localizedContext
                                                            .getString(
                                                                R.string
                                                                    .new_encounter_whatsapp_error
                                                            ),
                                                    type =
                                                        IdeSnackbarType.ERROR
                                                )
                                        )
                                }
                            }
                        }

                        navController
                            .popBackStack()
                    },

                    onLoadFailed = {

                        val localizedContext =
                            context.forLanguage(
                                appLanguage
                            )

                        coroutineScope.launch {

                            snackbarHostState
                                .showSnackbar(
                                    visuals =
                                        IdeSnackbarVisuals(
                                            message =
                                                localizedContext
                                                    .getString(
                                                        R.string
                                                            .new_encounter_loading_error
                                                    ),
                                            type =
                                                IdeSnackbarType.ERROR
                                        )
                                )
                        }
                    },

                    onSaveFailed = {

                        val localizedContext =
                            context.forLanguage(
                                appLanguage
                            )

                        coroutineScope.launch {

                            snackbarHostState
                                .showSnackbar(
                                    visuals =
                                        IdeSnackbarVisuals(
                                            message =
                                                localizedContext
                                                    .getString(
                                                        R.string
                                                            .new_encounter_save_error
                                                    ),
                                            type =
                                                IdeSnackbarType.ERROR
                                        )
                                )
                        }
                    },

                    onValidationFailed = {

                        val localizedContext =
                            context.forLanguage(
                                appLanguage
                            )

                        coroutineScope.launch {

                            snackbarHostState
                                .showSnackbar(
                                    visuals =
                                        IdeSnackbarVisuals(
                                            message =
                                                localizedContext
                                                    .getString(
                                                        R.string
                                                            .new_encounter_validation_invalid_phone
                                                    ),
                                            type =
                                                IdeSnackbarType.WARNING
                                        )
                                )
                        }
                    }
                )
            }

            composable<MissionLocations> {
                    backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<MissionLocations>()

                MissionLocationsScreen(
                    missionId =
                        route.missionId,

                    onBackClick = {
                        navController.popBackStack()
                    },

                    onSaved = {
                        navController.popBackStack()
                    }
                )
            }

            composable<MissionGroups> { backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<MissionGroups>()

                MissionGroupsScreen(
                    missionId =
                        route.missionId,

                    onBackClick = {
                        navController.popBackStack()
                    },

                    onSaved = {
                        navController.popBackStack()
                    }
                )
            }

            composable<MissionArea> { backStackEntry ->

                val route =
                    backStackEntry
                        .toRoute<MissionArea>()

                MissionAreaScreen(
                    missionId =
                        route.missionId,

                    onBackClick = {
                        navController.popBackStack()
                    },

                    onSaved = {
                        navController.popBackStack()
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

                        navController.navigate(
                            Metrics
                        ) {
                            launchSingleTop = true
                        }
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
                    .navigationBarsPadding()
                    .padding(
                        16.dp
                    )
        )
    }
}

private fun openWhatsAppConversation(
    context: android.content.Context,
    phoneDigits: String,
    message: String
): Boolean {

    val normalizedPhone =
        phoneDigits
            .filter(
                Char::isDigit
            )

    if (
        normalizedPhone.length !=
        11
    ) {
        return false
    }

    val fullPhone =
        "55$normalizedPhone"

    val encodedMessage =
        Uri.encode(
            message
        )

    /*
     * Primeiro tentamos o esquema nativo do WhatsApp.
     *
     * Não usamos resolveActivity() porque, a partir do Android 11,
     * a visibilidade de pacotes pode ocultar apps instalados quando
     * eles não estão declarados em <queries>.
     *
     * Em vez disso, tentamos abrir diretamente e tratamos a falha.
     */
    val nativeUri =
        Uri.parse(
            "whatsapp://send" +
                    "?phone=$fullPhone" +
                    "&text=$encodedMessage"
        )

    val whatsappPackages =
        listOf(
            "com.whatsapp",
            "com.whatsapp.w4b"
        )

    whatsappPackages
        .forEach { packageName ->

            val opened =
                runCatching {

                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            nativeUri
                        )
                            .apply {
                                setPackage(
                                    packageName
                                )

                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                )
                            }

                    context.startActivity(
                        intent
                    )

                    true
                }
                    .getOrDefault(
                        false
                    )

            if (
                opened
            ) {
                return true
            }
        }

    /*
     * Fallback:
     * se não houver WhatsApp/WhatsApp Business compatível,
     * usamos o link oficial wa.me. Nesse caso o Android pode
     * abrir o navegador ou outro manipulador compatível.
     */
    return runCatching {

        val webUri =
            Uri.parse(
                "https://wa.me/$fullPhone" +
                        "?text=$encodedMessage"
            )

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                webUri
            )
                .apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

        context.startActivity(
            intent
        )

        true
    }
        .getOrDefault(
            false
        )
}
