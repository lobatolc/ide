package br.com.ide.presentation.feature.missionplanning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle

@Composable
fun MissionPlanningScreen(
    missionId: String,
    onBackClick: () -> Unit,
    onLocationsClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onAreaClick: () -> Unit,
    viewModel: MissionPlanningViewModel =
        hiltViewModel()
) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    val lifecycleOwner =
        LocalLifecycleOwner.current

    // =========================================================
    // Carregamento inicial
    // =========================================================

    LaunchedEffect(
        missionId
    ) {

        viewModel.load(
            missionId
        )
    }

    // =========================================================
    // Atualizar ao retornar das subtelas
    // =========================================================

    DisposableEffect(
        lifecycleOwner,
        missionId
    ) {

        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {

                    viewModel.refresh(
                        missionId
                    )
                }
            }

        lifecycleOwner
            .lifecycle
            .addObserver(
                observer
            )

        onDispose {

            lifecycleOwner
                .lifecycle
                .removeObserver(
                    observer
                )
        }
    }

    // =========================================================
    // Loading
    // =========================================================

    if (
        uiState.isLoading &&
        uiState.missionName
            .isBlank()
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            verticalArrangement =
                Arrangement.Center,
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator()
        }

        return
    }

    MissionPlanningContent(
        uiState =
            uiState,

        onBackClick =
            onBackClick,

        onLocationsClick =
            onLocationsClick,

        onGroupsClick =
            onGroupsClick,

        onAreaClick =
            onAreaClick
    )
}

@Composable
private fun MissionPlanningContent(
    uiState: MissionPlanningUiState,
    onBackClick: () -> Unit,
    onLocationsClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onAreaClick: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                )
    ) {

        // =====================================================
        // Header
        // =====================================================

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IdeBackButton(
                onClick =
                    onBackClick,

                contentDescription =
                    stringResource(
                        R.string
                            .mission_planning_back
                    )
            )

            Spacer(
                modifier =
                    Modifier.size(
                        8.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                IdeScreenTitle(
                    text =
                        stringResource(
                            R.string
                                .mission_planning_title
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                IdeScreenSubtitle(
                    text =
                        uiState
                            .missionName
                            .ifBlank {

                                stringResource(
                                    R.string
                                        .mission_planning_subtitle
                                )
                            }
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        // =====================================================
        // Destaque
        // =====================================================

        PlanningHeroCard()

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        // =====================================================
        // Configuração
        // =====================================================

        Text(
            text =
                stringResource(
                    R.string
                        .mission_planning_configuration_title
                ),
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )

        Text(
            text =
                stringResource(
                    R.string
                        .mission_planning_configuration_description
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        // =====================================================
        // Locais
        // =====================================================

        LocationsPlanningCard(
            hasDepartureLocation =
                uiState
                    .hasDepartureLocation,

            hasReturnLocation =
                uiState
                    .hasReturnLocation,

            onClick =
                onLocationsClick
        )

        Spacer(
            modifier =
                Modifier.height(
                    14.dp
                )
        )

        // =====================================================
        // Grupos
        // =====================================================

        GroupsPlanningCard(
            groupCount =
                uiState.groupCount,

            groupedParticipantCount =
                uiState
                    .groupedParticipantCount,

            unassignedParticipantCount =
                uiState
                    .unassignedParticipantCount,

            onClick =
                onGroupsClick
        )

        Spacer(
            modifier =
                Modifier.height(
                    14.dp
                )
        )

        // =====================================================
        // Área de atuação
        // =====================================================

        AreaPlanningCard(
            hasDefinedArea =
                uiState
                    .hasDefinedArea,

            areaPointCount =
                uiState
                    .areaPointCount,

            onClick =
                onAreaClick
        )

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        // =====================================================
        // Informação
        // =====================================================

        PlanningInfoCard()

        Spacer(
            modifier =
                Modifier.height(
                    24.dp
                )
        )
    }
}

// =============================================================
// Locais
// =============================================================

@Composable
private fun LocationsPlanningCard(
    hasDepartureLocation: Boolean,
    hasReturnLocation: Boolean,
    onClick: () -> Unit
) {

    PlanningBaseCard(
        icon =
            if (
                hasDepartureLocation
            ) {
                Icons.Outlined.CheckCircle
            } else {
                Icons.Outlined.LocationOn
            },

        title =
            stringResource(
                R.string
                    .mission_planning_locations_title
            ),

        badge =
            stringResource(
                R.string
                    .mission_planning_required
            ),

        onClick =
            onClick
    ) {

        PlanningStatusRow(
            completed =
                hasDepartureLocation,

            icon =
                Icons.Outlined.LocationOn,

            text =
                if (
                    hasDepartureLocation
                ) {
                    stringResource(
                        R.string
                            .mission_planning_departure_defined
                    )
                } else {
                    stringResource(
                        R.string
                            .mission_planning_departure_not_defined
                    )
                }
        )

        Spacer(
            modifier =
                Modifier.height(
                    5.dp
                )
        )

        PlanningStatusRow(
            completed =
                hasReturnLocation,

            icon =
                Icons.Outlined.LocationOn,

            text =
                if (
                    hasReturnLocation
                ) {
                    stringResource(
                        R.string
                            .mission_planning_return_defined
                    )
                } else {
                    stringResource(
                        R.string
                            .mission_planning_return_not_defined
                    )
                }
        )
    }
}

// =============================================================
// Grupos
// =============================================================

@Composable
private fun GroupsPlanningCard(
    groupCount: Int,
    groupedParticipantCount: Int,
    unassignedParticipantCount: Int,
    onClick: () -> Unit
) {

    val hasGroups =
        groupCount > 0

    PlanningBaseCard(
        icon =
            if (
                hasGroups
            ) {
                Icons.Outlined.CheckCircle
            } else {
                Icons.Outlined.Groups
            },

        title =
            stringResource(
                R.string
                    .mission_planning_groups_title
            ),

        badge =
            stringResource(
                R.string
                    .mission_planning_optional
            ),

        onClick =
            onClick
    ) {

        if (
            !hasGroups
        ) {

            PlanningStatusRow(
                completed =
                    false,

                icon =
                    Icons.Outlined.Groups,

                text =
                    stringResource(
                        R.string
                            .mission_planning_groups_not_defined
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_planning_groups_everyone_together
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

        } else {

            PlanningStatusRow(
                completed =
                    true,

                text =
                    stringResource(
                        R.string
                            .mission_planning_groups_defined,
                        groupCount
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            PlanningStatusRow(
                completed =
                    groupedParticipantCount > 0,

                icon =
                    Icons.Outlined.PersonOutline,

                text =
                    stringResource(
                        R.string
                            .mission_planning_grouped_participants,
                        groupedParticipantCount
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            if (
                unassignedParticipantCount ==
                0
            ) {

                PlanningStatusRow(
                    completed =
                        true,

                    text =
                        stringResource(
                            R.string
                                .mission_planning_all_participants_grouped
                        )
                )

            } else {

                PlanningStatusRow(
                    completed =
                        false,

                    icon =
                        Icons.Outlined.PersonOutline,

                    text =
                        stringResource(
                            R.string
                                .mission_planning_unassigned_participants,
                            unassignedParticipantCount
                        )
                )
            }
        }
    }
}

// =============================================================
// Área de atuação
// =============================================================

@Composable
private fun AreaPlanningCard(
    hasDefinedArea: Boolean,
    areaPointCount: Int,
    onClick: () -> Unit
) {

    PlanningBaseCard(
        icon =
            if (
                hasDefinedArea
            ) {
                Icons.Outlined.CheckCircle
            } else {
                Icons.Outlined.Map
            },

        title =
            stringResource(
                R.string
                    .mission_planning_area_title
            ),

        badge =
            stringResource(
                R.string
                    .mission_planning_optional
            ),

        onClick =
            onClick
    ) {

        if (
            hasDefinedArea
        ) {

            PlanningStatusRow(
                completed =
                    true,

                icon =
                    Icons.Outlined.Map,

                text =
                    stringResource(
                        R.string
                            .mission_planning_area_defined
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_planning_area_points,
                        areaPointCount
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

        } else {

            PlanningStatusRow(
                completed =
                    false,

                icon =
                    Icons.Outlined.Map,

                text =
                    stringResource(
                        R.string
                            .mission_planning_area_free
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_planning_area_free_description
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

// =============================================================
// Card base
// =============================================================

@Composable
private fun PlanningBaseCard(
    icon: ImageVector,
    title: String,
    badge: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        18.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            Surface(
                shape =
                    RoundedCornerShape(
                        16.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Icon(
                    imageVector =
                        icon,
                    contentDescription =
                        null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    modifier =
                        Modifier
                            .padding(
                                12.dp
                            )
                            .size(
                                26.dp
                            )
                )
            }

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    Text(
                        text =
                            title,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.SemiBold,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                    )

                    PlanningBadge(
                        text =
                            badge
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                content()
            }

            Icon(
                imageVector =
                    Icons.Outlined
                        .ChevronRight,
                contentDescription =
                    null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

// =============================================================
// Status
// =============================================================

@Composable
private fun PlanningStatusRow(
    completed: Boolean,
    text: String,
    icon: ImageVector =
        if (
            completed
        ) {
            Icons.Outlined.CheckCircle
        } else {
            Icons.Outlined.LocationOn
        }
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                7.dp
            )
    ) {

        Icon(
            imageVector =
                icon,
            contentDescription =
                null,
            modifier =
                Modifier.size(
                    17.dp
                ),
            tint =
                if (
                    completed
                ) {
                    MaterialTheme
                        .colorScheme
                        .primary
                } else {
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                }
        )

        Text(
            text =
                text,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                if (
                    completed
                ) {
                    MaterialTheme
                        .colorScheme
                        .primary
                } else {
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                }
        )
    }
}

// =============================================================
// Hero
// =============================================================

@Composable
private fun PlanningHeroCard() {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                24.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        22.dp
                    )
        ) {

            Surface(
                shape =
                    RoundedCornerShape(
                        16.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.TaskAlt,
                    contentDescription =
                        null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onPrimary,
                    modifier =
                        Modifier
                            .padding(
                                12.dp
                            )
                            .size(
                                28.dp
                            )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_planning_hero_title
                    ),
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )

            Spacer(
                modifier =
                    Modifier.height(
                        6.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_planning_hero_description
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )
        }
    }
}

// =============================================================
// Badge
// =============================================================

@Composable
private fun PlanningBadge(
    text: String
) {

    Surface(
        shape =
            RoundedCornerShape(
                50
            ),
        color =
            MaterialTheme
                .colorScheme
                .secondaryContainer
    ) {

        Text(
            text =
                text,
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.SemiBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSecondaryContainer,
            modifier =
                Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 4.dp
                )
        )
    }
}

// =============================================================
// Informação
// =============================================================

@Composable
private fun PlanningInfoCard() {

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                18.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
    ) {

        Text(
            text =
                stringResource(
                    R.string
                        .mission_planning_edit_until_start
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            modifier =
                Modifier.padding(
                    16.dp
                )
        )
    }
}