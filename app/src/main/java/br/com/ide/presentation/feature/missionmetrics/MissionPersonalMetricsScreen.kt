package br.com.ide.presentation.feature.missionmetrics

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.ide.domain.model.MissionPersonalMetrics
import br.com.ide.presentation.components.IdeBackButton
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

data class MissionPersonalMetricsLabels(
    val title: String,
    val backContentDescription: String,
    val encounters: String,
    val visits: String,
    val prayers: String,
    val customActivity: String?,
    val bibleStudiesOffered: String,
    val bibleStudiesAccepted: String,
    val materialsDelivered: String,
    val surveysCompleted: String,
    val distance: String,
    val participationTime: String,
    val meterUnit: String,
    val kilometerUnit: String,
    val hourUnit: String,
    val minuteUnit: String
)

@Composable
fun MissionPersonalMetricsScreen(
    missionName: String,
    metrics: MissionPersonalMetrics,
    labels: MissionPersonalMetricsLabels,
    onBackClick: () -> Unit
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

        PersonalMetricsHeader(
            title =
                labels.title,
            missionName =
                missionName,
            backContentDescription =
                labels.backContentDescription,
            onBackClick =
                onBackClick
        )

        Spacer(
            modifier =
                Modifier.height(
                    24.dp
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            PersonalMetricCard(
                label =
                    labels.encounters,
                value =
                    metrics
                        .encounterCount
                        .toString(),
                modifier =
                    Modifier.weight(
                        1f
                    ),
                emphasized =
                    true
            )

            PersonalMetricCard(
                label =
                    labels.distance,
                value =
                    formatDistance(
                        meters =
                            metrics
                                .distanceMeters,
                        meterUnit =
                            labels.meterUnit,
                        kilometerUnit =
                            labels.kilometerUnit
                    ),
                modifier =
                    Modifier.weight(
                        1f
                    ),
                emphasized =
                    true
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        PersonalMetricCard(
            label =
                labels.participationTime,
            value =
                formatDuration(
                    totalSeconds =
                        metrics
                            .participationSeconds,
                    hourUnit =
                        labels.hourUnit,
                    minuteUnit =
                        labels.minuteUnit
                ),
            modifier =
                Modifier.fillMaxWidth(),
            emphasized =
                true
        )

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            PersonalMetricCard(
                label =
                    labels.visits,
                value =
                    metrics
                        .visitCount
                        .toString(),
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            PersonalMetricCard(
                label =
                    labels.prayers,
                value =
                    metrics
                        .prayerCount
                        .toString(),
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }

        labels
            .customActivity
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { customActivityLabel ->

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                PersonalMetricCard(
                    label =
                        customActivityLabel,
                    value =
                        metrics
                            .customActivityCount
                            .toString(),
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            PersonalMetricCard(
                label =
                    labels
                        .bibleStudiesOffered,
                value =
                    metrics
                        .offeredBibleStudyCount
                        .toString(),
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            PersonalMetricCard(
                label =
                    labels
                        .bibleStudiesAccepted,
                value =
                    metrics
                        .acceptedBibleStudyCount
                        .toString(),
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            PersonalMetricCard(
                label =
                    labels
                        .materialsDelivered,
                value =
                    metrics
                        .deliveredMaterialCount
                        .toString(),
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            PersonalMetricCard(
                label =
                    labels
                        .surveysCompleted,
                value =
                    metrics
                        .completedSurveyCount
                        .toString(),
                modifier =
                    Modifier.weight(
                        1f
                    )
            )
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
private fun PersonalMetricsHeader(
    title: String,
    missionName: String,
    backContentDescription: String,
    onBackClick: () -> Unit
) {

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
                backContentDescription
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

            Text(
                text =
                    title,
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

            if (
                missionName.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                Text(
                    text =
                        missionName,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PersonalMetricCard(
    label: String,
    value: String,
    modifier: Modifier =
        Modifier,
    emphasized: Boolean =
        false
) {

    Surface(
        modifier =
            modifier,
        shape =
            RoundedCornerShape(
                18.dp
            ),
        color =
            if (
                emphasized
            ) {
                MaterialTheme
                    .colorScheme
                    .primaryContainer
            } else {
                MaterialTheme
                    .colorScheme
                    .surface
            },
        tonalElevation =
            if (
                emphasized
            ) {
                0.dp
            } else {
                1.dp
            }
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {

            Text(
                text =
                    value,
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    if (
                        emphasized
                    ) {
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                    } else {
                        MaterialTheme
                            .colorScheme
                            .primary
                    }
            )

            Text(
                text =
                    label,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                fontWeight =
                    FontWeight.Medium,
                color =
                    if (
                        emphasized
                    ) {
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )
        }
    }
}

private fun formatDistance(
    meters: Double,
    meterUnit: String,
    kilometerUnit: String
): String {

    val safeMeters =
        meters
            .coerceAtLeast(
                0.0
            )

    return if (
        safeMeters <
        1000.0
    ) {

        val roundedMeters =
            safeMeters
                .roundToLong()

        "$roundedMeters $meterUnit"

    } else {

        val numberFormat =
            NumberFormat
                .getNumberInstance(
                    Locale.getDefault()
                )
                .apply {
                    minimumFractionDigits =
                        1
                    maximumFractionDigits =
                        2
                }

        val kilometers =
            safeMeters /
                    1000.0

        "${numberFormat.format(kilometers)} $kilometerUnit"
    }
}

private fun formatDuration(
    totalSeconds: Long,
    hourUnit: String,
    minuteUnit: String
): String {

    val safeSeconds =
        totalSeconds
            .coerceAtLeast(
                0L
            )

    val totalMinutes =
        safeSeconds /
                60L

    val hours =
        totalMinutes /
                60L

    val minutes =
        totalMinutes %
                60L

    return if (
        hours >
        0L
    ) {
        "$hours$hourUnit $minutes$minuteUnit"
    } else {
        "$minutes$minuteUnit"
    }
}
