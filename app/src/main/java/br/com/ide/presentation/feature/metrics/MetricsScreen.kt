package br.com.ide.presentation.feature.metrics

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.presentation.components.IdeBottomNavigation
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.BottomNavigationItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val XLSX_MIME =
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: MetricsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val exportLanguage = Locale.getDefault().language
    val exportSuccessMessage = stringResource(R.string.metrics_export_success)
    val exportErrorMessage = stringResource(R.string.metrics_export_error)
    val scope = rememberCoroutineScope()

    var pendingExportBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    val createDocumentLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(XLSX_MIME)
        ) { uri ->
            val bytes = pendingExportBytes
            pendingExportBytes = null

            if (uri == null || bytes == null) {
                return@rememberLauncherForActivityResult
            }

            val success = runCatching {
                context.contentResolver
                    .openOutputStream(uri)
                    ?.use { output -> output.write(bytes) }
                    ?: error("Unable to open destination")
            }.isSuccess

            Toast.makeText(
                context,
                if (success) {
                    exportSuccessMessage
                } else {
                    exportErrorMessage
                },
                Toast.LENGTH_LONG
            ).show()
        }

    val exportClick: () -> Unit = {
        if (!isExporting && uiState.selectedMissionIds.isNotEmpty()) {
            scope.launch {
                isExporting = true

                runCatching {
                    val snapshot = viewModel.buildExportSnapshot()
                    withContext(Dispatchers.Default) {
                        MetricsExcelExporter.create(
                            snapshot = snapshot,
                            language = exportLanguage
                        )
                    }
                }
                    .onSuccess { bytes ->
                        pendingExportBytes = bytes
                        createDocumentLauncher.launch(
                            "IDE_metricas_${System.currentTimeMillis()}.xlsx"
                        )
                    }
                    .onFailure {
                        Toast.makeText(
                            context,
                            exportErrorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                isExporting = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            IdeBottomNavigation(
                selectedItem = BottomNavigationItem.METRICS,
                onItemSelected = { item ->
                    when (item) {
                        BottomNavigationItem.HOME -> onHomeClick()
                        BottomNavigationItem.METRICS -> Unit
                        BottomNavigationItem.PROFILE -> onProfileClick()
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null &&
                    uiState.completedMissions.isEmpty() -> {
                MetricsError(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onRetry = {
                        viewModel.onEvent(MetricsEvent.Retry)
                    }
                )
            }

            else -> {
                MetricsContent(
                    uiState = uiState,
                    isExporting = isExporting,
                    onOpenSelector = {
                        viewModel.onEvent(MetricsEvent.OpenSelector)
                    },
                    onExportClick = exportClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (uiState.isSelectorOpen) {
        MissionSelectorDialog(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetricsContent(
    uiState: MetricsUiState,
    isExporting: Boolean,
    onOpenSelector: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = metricCards(uiState.summary)
    var selectedMetric by remember {
        mutableStateOf<MetricCardData?>(null)
    }

    val leftCards = remember(cards) {
        cards.filterIndexed { index, _ -> index % 2 == 0 }
    }
    val rightCards = remember(cards) {
        cards.filterIndexed { index, _ -> index % 2 != 0 }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                IdeScreenTitle(
                    text = stringResource(R.string.metrics_title)
                )
                Spacer(Modifier.height(4.dp))
                IdeScreenSubtitle(
                    text = stringResource(R.string.metrics_subtitle)
                )
            }
        }

        if (uiState.completedMissions.isEmpty()) {
            item {
                MetricsEmptyCard()
            }
            return@LazyColumn
        }

        item {
            MissionSelectionCard(
                uiState = uiState,
                onClick = onOpenSelector
            )
        }

        if (uiState.isCalculating) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        uiState.errorMessage?.let {
            item {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            Text(
                text = stringResource(R.string.metrics_results_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    leftCards.forEachIndexed { index, card ->
                        MetricCard(
                            data = card,
                            containerColor = metricContainerColor(index * 2),
                            onClick = { selectedMetric = card },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rightCards.forEachIndexed { index, card ->
                        MetricCard(
                            data = card,
                            containerColor = metricContainerColor(index * 2 + 1),
                            onClick = { selectedMetric = card },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            ExportCard(
                enabled = !uiState.isCalculating &&
                        !isExporting &&
                        uiState.selectedMissionIds.isNotEmpty(),
                isExporting = isExporting,
                onClick = onExportClick
            )
        }
    }

    selectedMetric?.let { metric ->
        MetricDetailsBottomSheet(
            data = metric,
            onDismiss = { selectedMetric = null }
        )
    }
}

@Composable
private fun MissionSelectionCard(
    uiState: MetricsUiState,
    onClick: () -> Unit
) {
    val selectedText =
        if (uiState.selectedMissionIds.size == 1) {
            stringResource(R.string.metrics_selected_one)
        } else {
            stringResource(
                R.string.metrics_selected_many,
                uiState.selectedMissionIds.size
            )
        }

    val rangeText = selectedDateRangeText(
        uiState.selectedStartDate,
        uiState.selectedEndDate
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.metrics_filter_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.metrics_only_completed),
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (rangeText.isNotBlank()) {
                Text(
                    text = rangeText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun metricCards(summary: MetricsSummary): List<MetricCardData> {
    val cards = mutableListOf(
        MetricCardData(
            key = "missions",
            label = stringResource(R.string.metrics_card_missions),
            value = summary.missionCount.toString(),
            icon = Icons.Outlined.Flag
        ),
        MetricCardData(
            key = "unique",
            label = stringResource(R.string.metrics_card_unique_missionaries),
            value = summary.uniqueMissionaryCount.toString(),
            icon = Icons.Outlined.Person
        ),
        MetricCardData(
            key = "participations",
            label = stringResource(R.string.metrics_card_participations),
            value = summary.participationCount.toString(),
            icon = Icons.Outlined.Groups
        ),
        MetricCardData(
            key = "groups",
            label = stringResource(R.string.mission_general_metrics_groups),
            value = summary.groupCount.toString(),
            icon = Icons.Outlined.Groups
        ),
        MetricCardData(
            key = "encounters",
            label = stringResource(R.string.mission_general_metrics_encounters),
            value = summary.encounterCount.toString(),
            icon = Icons.Outlined.Event
        ),
        MetricCardData(
            key = "prayers",
            label = stringResource(R.string.mission_general_metrics_prayers),
            value = summary.prayerCount.toString(),
            icon = Icons.Outlined.Schedule
        ),
        MetricCardData(
            key = "visits",
            label = stringResource(R.string.mission_general_metrics_visits),
            value = summary.visitCount.toString(),
            icon = Icons.Outlined.Place
        ),
        MetricCardData(
            key = "offered",
            label = stringResource(R.string.mission_general_metrics_bible_studies_offered),
            value = summary.offeredBibleStudyCount.toString(),
            icon = Icons.Outlined.MenuBook
        ),
        MetricCardData(
            key = "accepted",
            label = stringResource(R.string.mission_general_metrics_bible_studies_accepted),
            value = summary.acceptedBibleStudyCount.toString(),
            icon = Icons.Outlined.MenuBook
        ),
        MetricCardData(
            key = "materials",
            label = stringResource(R.string.mission_general_metrics_materials_delivered),
            value = summary.deliveredMaterialCount.toString(),
            icon = Icons.Outlined.Description
        ),
        MetricCardData(
            key = "surveys",
            label = stringResource(R.string.mission_general_metrics_surveys_completed),
            value = summary.completedSurveyCount.toString(),
            icon = Icons.Outlined.Description
        ),
        MetricCardData(
            key = "time",
            label = stringResource(R.string.mission_general_metrics_participation_time),
            value = formatDuration(summary.participationSeconds),
            icon = Icons.Outlined.Schedule
        ),
        MetricCardData(
            key = "distance",
            label = stringResource(R.string.mission_general_metrics_distance),
            value = formatDistance(summary.distanceMeters),
            icon = Icons.Outlined.MyLocation
        )
    )

    if (summary.customActivityCount > 0) {
        cards += MetricCardData(
            key = "other",
            label = stringResource(R.string.metrics_card_other_activities),
            value = summary.customActivityCount.toString(),
            icon = Icons.Outlined.BarChart
        )
    }

    return cards
}

@Composable
private fun MetricCard(
    data: MetricCardData,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f)
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = data.label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = data.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetricDetailsBottomSheet(
    data: MetricCardData,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 28.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Text(
                text = data.label,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = data.value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun metricContainerColor(index: Int): Color =
    when (index % 4) {
        0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        1 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.62f)
        2 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.58f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

@Composable
private fun ExportCard(
    enabled: Boolean,
    isExporting: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.metrics_export_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = stringResource(R.string.metrics_export_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null
                    )
                }

                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(
                        if (isExporting) {
                            R.string.metrics_export_building
                        } else {
                            R.string.metrics_export_button
                        }
                    )
                )
            }
        }
    }
}

@Composable
private fun MissionSelectorDialog(
    uiState: MetricsUiState,
    onEvent: (MetricsEvent) -> Unit
) {
    var periodMenuExpanded by remember {
        mutableStateOf(false)
    }

    Dialog(
        onDismissRequest = {
            onEvent(MetricsEvent.DismissSelector)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.80f),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 20.dp,
                            bottom = 16.dp
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.metrics_selector_title
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = stringResource(
                                    R.string.metrics_selector_subtitle
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                onEvent(MetricsEvent.DismissSelector)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(
                                    R.string.metrics_close_selector
                                )
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = {
                            onEvent(MetricsEvent.SearchChanged(it))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(
                                stringResource(
                                    R.string.metrics_search_hint
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null
                            )
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    periodMenuExpanded = true
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = stringResource(
                                        R.string.metrics_period_value,
                                        periodLabel(uiState.draftPeriod)
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = periodMenuExpanded,
                            onDismissRequest = {
                                periodMenuExpanded = false
                            }
                        ) {
                            MetricsPeriodFilter.entries.forEach { period ->
                                DropdownMenuItem(
                                    text = {
                                        Text(periodLabel(period))
                                    },
                                    onClick = {
                                        periodMenuExpanded = false
                                        onEvent(
                                            MetricsEvent.PeriodChanged(period)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = stringResource(
                            R.string.metrics_selection_limit,
                            METRICS_MAX_SELECTED_MISSIONS
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (
                            uiState.draftSelectedMissionIds.size >=
                            METRICS_MAX_SELECTED_MISSIONS
                        ) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (
                            uiState.draftSelectedMissionIds.size >=
                            METRICS_MAX_SELECTED_MISSIONS
                        ) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = 20.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = uiState.visibleMissions,
                            key = { it.id }
                        ) { mission ->
                            val selected =
                                mission.id in uiState.draftSelectedMissionIds

                            MissionCheckRow(
                                mission = mission,
                                selected = selected,
                                enabled =
                                    selected ||
                                            uiState.draftSelectedMissionIds.size <
                                            METRICS_MAX_SELECTED_MISSIONS,
                                onToggle = {
                                    onEvent(
                                        MetricsEvent.MissionToggled(
                                            mission.id
                                        )
                                    )
                                }
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {


                                Text(
                                    text = stringResource(
                                        R.string.metrics_selection_count_limit,
                                        uiState.draftSelectedMissionIds.size,
                                        METRICS_MAX_SELECTED_MISSIONS
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        onEvent(MetricsEvent.DismissSelector)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(
                                            android.R.string.cancel
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        onEvent(MetricsEvent.ApplySelection)
                                    },
                                    enabled =
                                        uiState.draftSelectedMissionIds.isNotEmpty(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                ) {
                                    Text(
                                        stringResource(
                                            R.string.metrics_apply
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionCheckRow(
    mission: MetricsMissionUiModel,
    selected: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onToggle
            )
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = selected,
            enabled = enabled,
            onCheckedChange = { onToggle() }
        )

        Surface(
            shape = RoundedCornerShape(13.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }

        Text(
            text = mission.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = formatMissionDate(mission.date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricsEmptyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
            Text(
                text = stringResource(R.string.metrics_empty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MetricsError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.metrics_loading_error),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.home_retry))
            }
        }
    }
}

@Composable
private fun periodLabel(period: MetricsPeriodFilter): String =
    stringResource(
        when (period) {
            MetricsPeriodFilter.ALL_TIME -> R.string.metrics_period_all
            MetricsPeriodFilter.CURRENT_YEAR -> R.string.metrics_period_current_year
            MetricsPeriodFilter.LAST_30_DAYS -> R.string.metrics_period_last_30_days
            MetricsPeriodFilter.LAST_90_DAYS -> R.string.metrics_period_last_90_days
        }
    )

@SuppressLint("NewApi")
@Composable
private fun selectedDateRangeText(
    start: LocalDateTime?,
    end: LocalDateTime?
): String {
    if (start == null || end == null) return ""

    val locale = Locale.getDefault()
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
    }

    val startText = start.format(formatter)
    val endText = end.format(formatter)

    return if (
        start.year == end.year &&
        start.monthValue == end.monthValue &&
        start.dayOfMonth == end.dayOfMonth
    ) {
        startText
    } else {
        "$startText – $endText"
    }
}

@SuppressLint("NewApi")
@Composable
private fun formatMissionDate(date: LocalDateTime): String {
    val locale = Locale.getDefault()
    val formatter = remember(locale) {
        DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
    }

    return date.format(formatter)
}

@Composable
private fun formatDistance(meters: Double): String {
    val safeMeters = meters.coerceAtLeast(0.0)
    return if (safeMeters >= 1000.0) {
        String.format(Locale.getDefault(), "%.1f km", safeMeters / 1000.0)
    } else {
        "${safeMeters.toLong()} m"
    }
}

@Composable
private fun formatDuration(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0L)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
}

private data class MetricCardData(
    val key: String,
    val label: String,
    val value: String,
    val icon: ImageVector
)
