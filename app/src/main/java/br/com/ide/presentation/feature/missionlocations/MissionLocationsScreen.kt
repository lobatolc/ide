package br.com.ide.presentation.feature.missionlocations

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.MissionLocation
import br.com.ide.domain.model.MissionStatus
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.map.MissionMap

@Composable
fun MissionLocationsScreen(
    missionId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MissionLocationsViewModel =
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

    MissionLocationsContent(
        uiState =
            uiState,
        onEvent =
            viewModel::onEvent,
        onBackClick =
            onBackClick
    )
}

@Composable
private fun MissionLocationsContent(
    uiState: MissionLocationsUiState,
    onEvent: (MissionLocationsEvent) -> Unit,
    onBackClick: () -> Unit
) {

    val isDarkTheme =
        MaterialTheme
            .colorScheme
            .background
            .luminance() < 0.5f

    val canEdit =
        uiState.missionStatus ==
                MissionStatus.PLANNING ||
                uiState.missionStatus ==
                MissionStatus.SCHEDULED

    val selectedLocation =
        when (
            uiState.selectedLocationType
        ) {

            MissionLocationType.DEPARTURE ->
                uiState.departureLocation

            MissionLocationType.RETURN ->
                uiState.returnLocation
        }

    val mapLatitude =
        selectedLocation
            ?.latitude
            ?: uiState.departureLocation
                ?.latitude
            ?: -1.2939

    val mapLongitude =
        selectedLocation
            ?.longitude
            ?: uiState.departureLocation
                ?.longitude
            ?: -47.9260

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
                            .mission_locations_back
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
                                .mission_locations_title
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
                    24.dp
                )
        )

        // =====================================================
        // Seletor
        // =====================================================

        LocationTypeSelector(
            selectedType =
                uiState.selectedLocationType,
            enabled =
                canEdit,
            onSelected = { type ->

                onEvent(
                    MissionLocationsEvent
                        .SelectLocationType(
                            type
                        )
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        // =====================================================
        // Card do mapa
        // =====================================================

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
                            .surface
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation =
                        2.dp
                )
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            16.dp
                        )
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {

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
                                if (
                                    uiState
                                        .selectedLocationType ==
                                    MissionLocationType
                                        .DEPARTURE
                                ) {
                                    Icons.Outlined.Flag
                                } else {
                                    Icons.Outlined.Place
                                },
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

                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    ) {

                        Text(
                            text =
                                if (
                                    uiState
                                        .selectedLocationType ==
                                    MissionLocationType
                                        .DEPARTURE
                                ) {
                                    stringResource(
                                        R.string
                                            .mission_locations_map_departure_title
                                    )
                                } else {
                                    stringResource(
                                        R.string
                                            .mission_locations_map_return_title
                                    )
                                },
                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,
                            fontWeight =
                                FontWeight
                                    .SemiBold
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
                                    uiState
                                        .selectedLocationType ==
                                    MissionLocationType
                                        .DEPARTURE
                                ) {
                                    stringResource(
                                        R.string
                                            .mission_locations_departure_help
                                    )
                                } else {
                                    stringResource(
                                        R.string
                                            .mission_locations_return_help
                                    )
                                },
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

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    18.dp
                                )
                            )
                ) {

                    MissionMap(
                        latitude =
                            mapLatitude,
                        longitude =
                            mapLongitude,
                        selectedLatitude =
                            selectedLocation
                                ?.latitude,
                        selectedLongitude =
                            selectedLocation
                                ?.longitude,
                        isDarkTheme =
                            isDarkTheme,
                        recenterKey =
                            selectedLocation
                                ?.let {

                                    "${uiState.selectedLocationType}-${it.latitude}-${it.longitude}"
                                },
                        onMapClick = {
                                latitude,
                                longitude ->

                            if (
                                !canEdit
                            ) {
                                return@MissionMap
                            }

                            onEvent(
                                MissionLocationsEvent
                                    .LocationSelected(
                                        latitude =
                                            latitude,
                                        longitude =
                                            longitude
                                    )
                            )
                        }
                    )
                }

                if (
                    uiState.isGeocoding
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                14.dp
                            )
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.Center
                    ) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    18.dp
                                ),
                            strokeWidth =
                                2.dp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )

                        Spacer(
                            modifier =
                                Modifier.size(
                                    10.dp
                                )
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_locations_geocoding
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary,
                            fontWeight =
                                FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        // =====================================================
        // Locais definidos
        // =====================================================

        Text(
            text =
                stringResource(
                    R.string
                        .mission_locations_defined_title
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
                        .mission_locations_defined_subtitle
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

        LocationCard(
            title =
                stringResource(
                    R.string
                        .mission_locations_departure
                ),
            location =
                uiState.departureLocation,
            required =
                true
        )

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        LocationCard(
            title =
                stringResource(
                    R.string
                        .mission_locations_return
                ),
            location =
                uiState.returnLocation,
            required =
                false,
            removable =
                canEdit &&
                        uiState.returnLocation != null,
            onRemove = {

                onEvent(
                    MissionLocationsEvent
                        .RemoveReturnLocation
                )
            }
        )

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
        // Salvar / somente leitura
        // =====================================================

        if (
            canEdit
        ) {

            IdePrimaryButton(
                text =
                    stringResource(
                        R.string
                            .mission_locations_save
                    ),
                onClick = {

                    onEvent(
                        MissionLocationsEvent.Save
                    )
                },
                isLoading =
                    uiState.isSaving,
                enabled =
                    !uiState.isSaving &&
                            !uiState.isGeocoding,
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
                                .mission_locations_read_only
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

@Composable
private fun LocationTypeSelector(
    selectedType: MissionLocationType,
    enabled: Boolean,
    onSelected: (MissionLocationType) -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant,
                    shape =
                        RoundedCornerShape(
                            18.dp
                        )
                )
                .padding(
                    4.dp
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                4.dp
            )
    ) {

        LocationSelectorItem(
            text =
                stringResource(
                    R.string
                        .mission_locations_departure
                ),
            icon =
                Icons.Outlined.Flag,
            selected =
                selectedType ==
                        MissionLocationType.DEPARTURE,
            enabled =
                enabled,
            modifier =
                Modifier.weight(
                    1f
                ),
            onClick = {

                onSelected(
                    MissionLocationType.DEPARTURE
                )
            }
        )

        LocationSelectorItem(
            text =
                stringResource(
                    R.string
                        .mission_locations_return
                ),
            icon =
                Icons.Outlined.LocationOn,
            selected =
                selectedType ==
                        MissionLocationType.RETURN,
            enabled =
                enabled,
            modifier =
                Modifier.weight(
                    1f
                ),
            onClick = {

                onSelected(
                    MissionLocationType.RETURN
                )
            }
        )
    }
}

@Composable
private fun LocationSelectorItem(
    text: String,
    icon:
    androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .clickable(
                    enabled =
                        enabled,
                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                14.dp
            ),
        color =
            if (
                selected
            ) {
                MaterialTheme
                    .colorScheme
                    .primary
            } else {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
            }
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 11.dp,
                        horizontal = 12.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.Center
        ) {

            Icon(
                imageVector =
                    icon,
                contentDescription =
                    null,
                tint =
                    if (
                        selected
                    ) {
                        MaterialTheme
                            .colorScheme
                            .onPrimary
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    },
                modifier =
                    Modifier.size(
                        19.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.size(
                        7.dp
                    )
            )

            Text(
                text =
                    text,
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    if (
                        selected
                    ) {
                        MaterialTheme
                            .colorScheme
                            .onPrimary
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )
        }
    }
}

@Composable
private fun LocationCard(
    title: String,
    location: MissionLocation?,
    required: Boolean,
    removable: Boolean = false,
    onRemove: () -> Unit = {}
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                20.dp
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
                    1.dp
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
                    14.dp
                )
        ) {

            Surface(
                shape =
                    RoundedCornerShape(
                        14.dp
                    ),
                color =
                    if (
                        location != null
                    ) {
                        MaterialTheme
                            .colorScheme
                            .primaryContainer
                    } else {
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    }
            ) {

                Icon(
                    imageVector =
                        if (
                            location != null
                        ) {
                            Icons.Outlined.CheckCircle
                        } else {
                            Icons.Outlined.LocationOn
                        },
                    contentDescription =
                        null,
                    tint =
                        if (
                            location != null
                        ) {
                            MaterialTheme
                                .colorScheme
                                .primary
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        },
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
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            3.dp
                        )
                )

                if (
                    location == null
                ) {

                    Text(
                        text =
                            if (
                                required
                            ) {
                                stringResource(
                                    R.string
                                        .mission_locations_not_defined_required
                                )
                            } else {
                                stringResource(
                                    R.string
                                        .mission_locations_not_defined_optional
                                )
                            },
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                } else {

                    val locationName =
                        location.name.ifBlank {
                            stringResource(
                                R.string
                                    .mission_locations_selected_location
                            )
                        }

                    Text(
                        text =
                            locationName,
                        style =
                            MaterialTheme
                                .typography
                                .bodyLarge,
                        fontWeight =
                            FontWeight.Medium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                    )

                    if (
                        location.address
                            .isNotBlank()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    3.dp
                                )
                        )

                        Text(
                            text =
                                location.address,
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

            if (
                removable
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.DeleteOutline,
                    contentDescription =
                        stringResource(
                            R.string
                                .mission_locations_remove_return
                        ),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .error,
                    modifier =
                        Modifier
                            .size(
                                24.dp
                            )
                            .clickable {
                                onRemove()
                            }
                )
            }
        }
    }
}