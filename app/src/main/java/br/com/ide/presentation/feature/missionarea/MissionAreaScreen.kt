package br.com.ide.presentation.feature.missionarea

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.map.MissionMap

@Composable
fun MissionAreaScreen(
    missionId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MissionAreaViewModel =
        hiltViewModel()
) {

    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        missionId
    ) {

        viewModel.load(
            missionId
        )
    }

    LaunchedEffect(
        uiState.isSaved
    ) {

        if (
            uiState.isSaved
        ) {

            onSaved()
        }
    }

    if (
        uiState.isLoading
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            CircularProgressIndicator()
        }

        return
    }

    MissionAreaContent(
        uiState =
            uiState,

        onBackClick =
            onBackClick,

        onEvent =
            viewModel::onEvent
    )
}

@Composable
private fun MissionAreaContent(
    uiState: MissionAreaUiState,
    onBackClick: () -> Unit,
    onEvent: (MissionAreaEvent) -> Unit
) {

    val isDarkTheme =
        MaterialTheme
            .colorScheme
            .background
            .luminance() < 0.5f

    val scrollState =
        rememberScrollState()

    var isMapInteracting by
    remember {
        mutableStateOf(
            false
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(
                    state =
                        scrollState,
                    enabled =
                        !isMapInteracting
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
                            .mission_area_back
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
                                .mission_area_title
                        )
                )

                if (
                    uiState.missionName
                        .isNotBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                2.dp
                            )
                    )

                    IdeScreenSubtitle(
                        text =
                            uiState.missionName
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    26.dp
                )
        )

        // =====================================================
        // Hero
        // =====================================================

        MissionAreaHeroCard()

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        // =====================================================
        // Tipo da área
        // =====================================================

        Text(
            text =
                stringResource(
                    R.string
                        .mission_area_mode_title
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
                        .mission_area_mode_description
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
                    16.dp
                )
        )

        AreaModeCard(
            title =
                stringResource(
                    R.string
                        .mission_area_free_title
                ),

            description =
                stringResource(
                    R.string
                        .mission_area_free_description
                ),

            icon =
                Icons.Outlined.Language,

            selected =
                uiState.mode ==
                        MissionAreaMode.FREE,

            enabled =
                uiState.canEdit,

            onClick = {

                onEvent(
                    MissionAreaEvent
                        .SelectMode(
                            MissionAreaMode.FREE
                        )
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        AreaModeCard(
            title =
                stringResource(
                    R.string
                        .mission_area_defined_title
                ),

            description =
                stringResource(
                    R.string
                        .mission_area_defined_description
                ),

            icon =
                Icons.Outlined.Map,

            selected =
                uiState.mode ==
                        MissionAreaMode.DEFINED,

            enabled =
                uiState.canEdit,

            onClick = {

                onEvent(
                    MissionAreaEvent
                        .SelectMode(
                            MissionAreaMode.DEFINED
                        )
                )
            }
        )

        // =====================================================
        // Área delimitada
        // =====================================================

        if (
            uiState.mode ==
            MissionAreaMode.DEFINED
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_area_map_title
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
                            .mission_area_map_description
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
                        16.dp
                    )
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
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

                Column {

                    MissionMap(
                        latitude =
                            uiState.mapLatitude,

                        longitude =
                            uiState.mapLongitude,

                        zoom =
                            15.0,

                        polygonPoints =
                            uiState.polygonPoints,

                        isDarkTheme =
                            isDarkTheme,

                        onInteractionChanged = {
                            isMapInteracting =
                                it
                        },

                        onMapClick = {
                                latitude,
                                longitude ->

                            if (
                                !uiState.canEdit
                            ) {
                                return@MissionMap
                            }

                            onEvent(
                                MissionAreaEvent
                                    .AddPoint(
                                        latitude =
                                            latitude,

                                        longitude =
                                            longitude
                                    )
                            )
                        }
                    )

                    AreaPointStatus(
                        pointCount =
                            uiState
                                .polygonPoints
                                .size
                    )
                }
            }

            if (
                uiState.canEdit
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                AreaActions(
                    canUndo =
                        uiState.canUndo,

                    onUndo = {

                        onEvent(
                            MissionAreaEvent
                                .UndoLastPoint
                        )
                    },

                    onClear = {

                        onEvent(
                            MissionAreaEvent
                                .ClearArea
                        )
                    }
                )
            }
        }

        // =====================================================
        // Erro
        // =====================================================

        uiState.errorMessage
            ?.let { errorRes ->

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            errorRes
                        ),
                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        // =====================================================
        // Salvar
        // =====================================================

        if (
            uiState.canEdit
        ) {

            IdePrimaryButton(
                text =
                    if (
                        uiState.mode ==
                        MissionAreaMode.FREE
                    ) {

                        stringResource(
                            R.string
                                .mission_area_save_free
                        )

                    } else {

                        stringResource(
                            R.string
                                .mission_area_save_defined
                        )
                    },

                onClick = {

                    onEvent(
                        MissionAreaEvent.Save
                    )
                },

                isLoading =
                    uiState.isSaving,

                enabled =
                    !uiState.isSaving &&
                            (
                                    uiState.mode ==
                                            MissionAreaMode.FREE ||
                                            uiState.canSaveDefinedArea
                                    ),

                modifier =
                    Modifier.fillMaxWidth()
            )

        } else {

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
                                .mission_area_read_only
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

        Spacer(
            modifier =
                Modifier.height(
                    24.dp
                )
        )
    }
}

// =============================================================
// Hero
// =============================================================

@Composable
private fun MissionAreaHeroCard() {

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

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        20.dp
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
                        .primary
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Map,
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

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_area_hero_title
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
                            5.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_area_hero_description
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
}

// =============================================================
// Seleção livre / delimitada
// =============================================================

@Composable
private fun AreaModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled =
                        enabled,
                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        border =
            if (
                selected
            ) {
                BorderStroke(
                    width =
                        2.dp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            } else {
                null
            },
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
                        selected
                    ) {
                        2.dp
                    } else {
                        1.dp
                    }
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            RadioButton(
                selected =
                    selected,
                onClick =
                    if (
                        enabled
                    ) {
                        onClick
                    } else {
                        null
                    }
            )

            Spacer(
                modifier =
                    Modifier.size(
                        8.dp
                    )
            )

            Surface(
                shape =
                    RoundedCornerShape(
                        14.dp
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
                                10.dp
                            )
                            .size(
                                24.dp
                            )
                )
            }

            Spacer(
                modifier =
                    Modifier.size(
                        14.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
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

                Spacer(
                    modifier =
                        Modifier.height(
                            3.dp
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
            }
        }
    }
}

// =============================================================
// Status dos pontos
// =============================================================

@Composable
private fun AreaPointStatus(
    pointCount: Int
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    16.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {

        Surface(
            shape =
                CircleShape,
            color =
                MaterialTheme
                    .colorScheme
                    .primaryContainer
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Place,
                contentDescription =
                    null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary,
                modifier =
                    Modifier
                        .padding(
                            8.dp
                        )
                        .size(
                            20.dp
                        )
            )
        }

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_area_point_count,
                        pointCount
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            Spacer(
                modifier =
                    Modifier.height(
                        2.dp
                    )
            )

            Text(
                text =
                    if (
                        pointCount >= 3
                    ) {

                        stringResource(
                            R.string
                                .mission_area_perimeter_ready
                        )

                    } else {

                        stringResource(
                            R.string
                                .mission_area_minimum_points
                        )
                    },
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    if (
                        pointCount >= 3
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
}

// =============================================================
// Desfazer / limpar
// =============================================================

@Composable
private fun AreaActions(
    canUndo: Boolean,
    onUndo: () -> Unit,
    onClear: () -> Unit
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        OutlinedButton(
            onClick =
                onUndo,
            enabled =
                canUndo,
            modifier =
                Modifier.weight(
                    1f
                ),
            shape =
                RoundedCornerShape(
                    16.dp
                )
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Undo,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(
                        18.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.size(
                        6.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_area_undo
                    )
            )
        }

        OutlinedButton(
            onClick =
                onClear,
            enabled =
                canUndo,
            modifier =
                Modifier.weight(
                    1f
                ),
            shape =
                RoundedCornerShape(
                    16.dp
                ),
            border =
                BorderStroke(
                    width =
                        1.dp,
                    color =
                        if (
                            canUndo
                        ) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            MaterialTheme
                                .colorScheme
                                .outlineVariant
                        }
                ),
            colors =
                ButtonDefaults
                    .outlinedButtonColors(
                        contentColor =
                            MaterialTheme
                                .colorScheme
                                .error,
                        disabledContentColor =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.DeleteOutline,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(
                        18.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.size(
                        6.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_area_clear
                    )
            )
        }
    }
}
