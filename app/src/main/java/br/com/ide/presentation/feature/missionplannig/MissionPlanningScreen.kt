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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle

@Composable
fun MissionPlanningScreen(
    missionId: String,
    onBackClick: () -> Unit,
    onLocationsClick: () -> Unit
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
                        R.string.mission_planning_back
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
                            R.string.mission_planning_title
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
                        stringResource(
                            R.string.mission_planning_subtitle
                        )
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
        // Card principal
        // =====================================================

        PlanningHeroCard()

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        Text(
            text =
                stringResource(
                    R.string.mission_planning_configuration_title
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
                    R.string.mission_planning_configuration_description
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

        PlanningOptionCard(
            icon =
                Icons.Outlined.LocationOn,

            title =
                stringResource(
                    R.string.mission_planning_locations_title
                ),

            description =
                stringResource(
                    R.string.mission_planning_locations_description
                ),

            badge =
                stringResource(
                    R.string.mission_planning_required
                ),

            enabled =
                true,

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

        PlanningOptionCard(
            icon =
                Icons.Outlined.Groups,

            title =
                stringResource(
                    R.string.mission_planning_groups_title
                ),

            description =
                stringResource(
                    R.string.mission_planning_groups_description
                ),

            badge =
                stringResource(
                    R.string.mission_planning_optional
                ),

            enabled =
                false,

            onClick = {}
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

        PlanningOptionCard(
            icon =
                Icons.Outlined.Map,

            title =
                stringResource(
                    R.string.mission_planning_area_title
                ),

            description =
                stringResource(
                    R.string.mission_planning_area_description
                ),

            badge =
                stringResource(
                    R.string.mission_planning_optional
                ),

            enabled =
                false,

            onClick = {}
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
                        R.string.mission_planning_hero_title
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
                        R.string.mission_planning_hero_description
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

@Composable
private fun PlanningOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled =
                        enabled
                ) {
                    onClick()
                },
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
                    if (
                        enabled
                    ) {
                        2.dp
                    } else {
                        0.dp
                    }
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
                            5.dp
                        )
                )

                Text(
                    text =
                        description,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                if (
                    !enabled
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                6.dp
                            )
                    )

                    Text(
                        text =
                            stringResource(
                                R.string.mission_planning_coming_soon
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }

            if (
                enabled
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.ChevronRight,
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
}

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
                    R.string.mission_planning_edit_until_start
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