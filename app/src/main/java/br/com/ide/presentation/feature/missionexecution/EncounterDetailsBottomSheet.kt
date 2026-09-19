package br.com.ide.presentation.feature.missionexecution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.EncounterAgeGroup
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionEncounterMaterial
import br.com.ide.domain.model.MissionMaterialType
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncounterDetailsBottomSheet(
    encounter: MissionEncounter,
    onDismiss: () -> Unit,
    onWhatsAppClick: () -> Unit = {},
    onTraceRouteClick: () -> Unit = {},
    customActivityPerformedLabel: String? = null
) {

    val simpleActivities =
        encounter
            .performedActivities
            .filter {
                it ==
                        MissionActivityType.VISIT ||
                        it ==
                        MissionActivityType.PRAYER ||
                        it ==
                        MissionActivityType.OTHER
            }

    val hasPhone =
        encounter
            .phoneDigits
            ?.filter(
                Char::isDigit
            )
            ?.length ==
                11

    val hasLocation =
        encounter.latitude !=
                null &&
                encounter.longitude !=
                null

    ModalBottomSheet(
        onDismissRequest =
            onDismiss
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 32.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            EncounterHeader(
                encounter =
                    encounter
            )

            EncounterPersonSection(
                encounter =
                    encounter
            )

            if (
                simpleActivities.isNotEmpty()
            ) {

                HorizontalDivider()

                EncounterSectionTitle(
                    text =
                        stringResource(
                            R.string
                                .new_encounter_actions_section
                        )
                )

                simpleActivities
                    .forEach { activity ->

                        EncounterValueCard(
                            value =
                                activityLabel(
                                    activity =
                                        activity,
                                    customActivityPerformedLabel =
                                        customActivityPerformedLabel
                                )
                        )
                    }
            }

            if (
                encounter.bibleStudyStatus !=
                BibleStudyStatus.NOT_OFFERED
            ) {

                HorizontalDivider()

                EncounterSectionTitle(
                    text =
                        stringResource(
                            R.string
                                .new_encounter_bible_study_title
                        )
                )

                EncounterValueCard(
                    value =
                        bibleStudyStatusLabel(
                            encounter
                                .bibleStudyStatus
                        )
                )
            }

            if (
                encounter.materials.isNotEmpty()
            ) {

                HorizontalDivider()

                EncounterSectionTitle(
                    text =
                        stringResource(
                            R.string
                                .new_encounter_materials_title
                        )
                )

                encounter
                    .materials
                    .forEach { material ->

                        EncounterMaterialRow(
                            material =
                                material
                        )
                    }
            }

            if (
                encounter.surveyAnswers.isNotEmpty()
            ) {

                HorizontalDivider()

                EncounterSectionTitle(
                    text =
                        stringResource(
                            R.string
                                .new_encounter_survey_title
                        )
                )

                encounter
                    .surveyAnswers
                    .forEach { surveyAnswer ->

                        EncounterDetailRow(
                            label =
                                surveyAnswer
                                    .question,
                            value =
                                surveyAnswer
                                    .answer
                        )
                    }
            }

            if (
                encounter.acceptedFollowUp
            ) {

                HorizontalDivider()

                EncounterSectionTitle(
                    text =
                        stringResource(
                            R.string
                                .new_encounter_follow_up_section
                        )
                )

                EncounterStatusPill(
                    text =
                        stringResource(
                            R.string
                                .new_encounter_follow_up_accepted
                        )
                )
            }

            encounter
                .notes
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { notes ->

                    HorizontalDivider()

                    EncounterDetailRow(
                        label =
                            stringResource(
                                R.string
                                    .new_encounter_notes
                            ),
                        value =
                            notes
                    )
                }

            if (
                hasPhone ||
                hasLocation
            ) {

                HorizontalDivider()

                if (
                    hasPhone
                ) {

                    Button(
                        onClick =
                            onWhatsAppClick,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Phone,
                            contentDescription =
                                null
                        )

                        Spacer(
                            modifier =
                                Modifier.size(
                                    8.dp
                                )
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .new_encounter_contact_now
                                )
                        )
                    }
                }

                if (
                    hasLocation
                ) {

                    val routeButtonContent:
                            @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {

                        Icon(
                            imageVector =
                                Icons.Outlined.LocationOn,
                            contentDescription =
                                null
                        )

                        Spacer(
                            modifier =
                                Modifier.size(
                                    8.dp
                                )
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_participant_trace_route
                                )
                        )
                    }

                    if (
                        hasPhone
                    ) {

                        OutlinedButton(
                            onClick =
                                onTraceRouteClick,
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            content =
                                routeButtonContent
                        )

                    } else {

                        Button(
                            onClick =
                                onTraceRouteClick,
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            content =
                                routeButtonContent
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )
        }
    }
}

@Composable
private fun EncounterHeader(
    encounter: MissionEncounter
) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        androidx.compose.foundation.layout.Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            Box(
                modifier =
                    Modifier
                        .size(
                            18.dp
                        )
                        .background(
                            color =
                                encounter
                                    .groupColorHex
                                    .toEncounterColorOrNull()
                                    ?: MaterialTheme
                                        .colorScheme
                                        .primary,
                            shape =
                                CircleShape
                        )
            )

            Text(
                text =
                    encounter
                        .personName
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: stringResource(
                            R.string
                                .new_encounter_person_section
                        ),
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )
        }

        if (
            encounter.isAtPersonHome
        ) {

            Text(
                text =
                    stringResource(
                        R.string
                            .new_encounter_at_person_home
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}

@Composable
private fun EncounterPersonSection(
    encounter: MissionEncounter
) {

    val ageGroup =
        encounter.ageGroup

    val phone =
        encounter
            .phoneDigits
            ?.takeIf {
                it.isNotBlank()
            }

    val address =
        encounter
            .address
            ?.takeIf {
                it.isNotBlank()
            }

    if (
        ageGroup == null &&
        phone == null &&
        address == null
    ) {
        return
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        EncounterSectionTitle(
            text =
                stringResource(
                    R.string
                        .new_encounter_person_section
                )
        )

        ageGroup
            ?.let {

                EncounterInfoCard(
                    icon =
                        Icons.Outlined.Person,
                    value =
                        encounterAgeLabel(
                            ageGroup
                        )
                )
            }

        phone
            ?.let {

                EncounterInfoCard(
                    icon =
                        Icons.Outlined.Phone,
                    value =
                        formatBrazilianPhone(
                            phone
                        )
                )
            }

        address
            ?.let {

                EncounterInfoCard(
                    icon =
                        Icons.Outlined.Home,
                    value =
                        address
                )
            }
    }
}

@Composable
private fun EncounterSectionTitle(
    text: String
) {

    Text(
        text =
            text,
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
}

@Composable
private fun EncounterDetailRow(
    label: String,
    value: String
) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                4.dp
            )
    ) {

        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Text(
            text =
                value,
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
    }
}

@Composable
private fun EncounterInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {

    Surface(
        shape =
            RoundedCornerShape(
                14.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
    ) {

        androidx.compose.foundation.layout.Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 11.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            Icon(
                imageVector =
                    icon,
                contentDescription =
                    null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text =
                    value,
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
        }
    }
}

@Composable
private fun EncounterValueCard(
    value: String
) {

    Surface(
        shape =
            RoundedCornerShape(
                14.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
    ) {

        Text(
            text =
                value,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 11.dp
                    ),
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            fontWeight =
                FontWeight.Medium
        )
    }
}

@Composable
private fun EncounterStatusPill(
    text: String
) {

    Surface(
        shape =
            RoundedCornerShape(
                999.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .primaryContainer,
        contentColor =
            MaterialTheme
                .colorScheme
                .onPrimaryContainer
    ) {

        Text(
            text =
                text,
            modifier =
                Modifier
                    .padding(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    ),
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun EncounterMaterialRow(
    material: MissionEncounterMaterial
) {

    EncounterDetailRow(
        label =
            materialLabel(
                material
            ),
        value =
            material
                .quantity
                .toString()
    )
}

@Composable
private fun encounterAgeLabel(
    ageGroup: EncounterAgeGroup
): String {

    return when (
        ageGroup
    ) {

        EncounterAgeGroup.CHILD ->
            stringResource(
                R.string
                    .new_encounter_age_child
            )

        EncounterAgeGroup.TEENAGER ->
            stringResource(
                R.string
                    .new_encounter_age_teenager
            )

        EncounterAgeGroup.YOUTH ->
            stringResource(
                R.string
                    .new_encounter_age_youth
            )

        EncounterAgeGroup.ADULT ->
            stringResource(
                R.string
                    .new_encounter_age_adult
            )

        EncounterAgeGroup.ELDERLY ->
            stringResource(
                R.string
                    .new_encounter_age_elderly
            )
    }
}

@Composable
private fun activityLabel(
    activity: MissionActivityType,
    customActivityPerformedLabel: String? = null
): String {

    return when (
        activity
    ) {

        MissionActivityType.VISIT ->
            stringResource(
                R.string
                    .new_encounter_activity_visit
            )

        MissionActivityType.PRAYER ->
            stringResource(
                R.string
                    .new_encounter_activity_prayer
            )

        MissionActivityType.OTHER ->
            customActivityPerformedLabel
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: stringResource(
                    R.string
                        .new_encounter_activity_other
                )

        MissionActivityType.BIBLE_STUDY ->
            stringResource(
                R.string
                    .new_encounter_bible_study_title
            )

        MissionActivityType.MATERIAL_DELIVERY ->
            stringResource(
                R.string
                    .new_encounter_materials_title
            )

        MissionActivityType.OPINION_SURVEY ->
            stringResource(
                R.string
                    .new_encounter_survey_title
            )
    }
}

@Composable
private fun bibleStudyStatusLabel(
    status: BibleStudyStatus
): String {

    return when (
        status
    ) {

        BibleStudyStatus.NOT_OFFERED ->
            stringResource(
                R.string
                    .new_encounter_bible_study_not_offered
            )

        BibleStudyStatus.OFFERED ->
            stringResource(
                R.string
                    .new_encounter_bible_study_offered
            )

        BibleStudyStatus.ACCEPTED ->
            stringResource(
                R.string
                    .new_encounter_bible_study_accepted
            )
    }
}

@Composable
private fun materialLabel(
    material: MissionEncounterMaterial
): String {

    return when (
        material.type
    ) {

        MissionMaterialType.BOOK ->
            stringResource(
                R.string
                    .new_encounter_material_book
            )

        MissionMaterialType.MAGAZINE ->
            stringResource(
                R.string
                    .new_encounter_material_magazine
            )

        MissionMaterialType.LEAFLET ->
            stringResource(
                R.string
                    .new_encounter_material_leaflet
            )

        MissionMaterialType.OTHER ->
            material
                .customName
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: stringResource(
                    R.string
                        .new_encounter_material_other
                )
    }
}

private fun formatBrazilianPhone(
    phoneDigits: String
): String {

    val digits =
        phoneDigits
            .filter(
                Char::isDigit
            )
            .takeLast(
                11
            )

    if (
        digits.length !=
        11
    ) {
        return phoneDigits
    }

    return buildString {
        append("(")
        append(
            digits.substring(
                0,
                2
            )
        )
        append(") ")
        append(
            digits.substring(
                2,
                3
            )
        )
        append(" ")
        append(
            digits.substring(
                3,
                7
            )
        )
        append("-")
        append(
            digits.substring(
                7
            )
        )
    }
}

private fun String?.toEncounterColorOrNull():
        Color? {

    val value =
        this
            ?.takeIf {
                it.matches(
                    Regex(
                        "^#[0-9A-Fa-f]{6}$"
                    )
                )
            }
            ?: return null

    return runCatching {
        Color(
            value.toColorInt()
        )
    }
        .getOrNull()
}
