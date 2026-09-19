package br.com.ide.presentation.feature.newencounter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.EncounterAgeGroup
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionSurveyQuestion
import br.com.ide.domain.model.SurveyQuestionType
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle

@Composable
fun NewEncounterScreen(
    missionId: String,
    onBackClick: () -> Unit,
    onEncounterSaved: (
        NewEncounterEffect.EncounterSaved
    ) -> Unit,
    onLoadFailed: () -> Unit = {},
    onSaveFailed: () -> Unit = {},
    onValidationFailed: (
        NewEncounterValidationError
    ) -> Unit = {},
    viewModel: NewEncounterViewModel =
        hiltViewModel()
) {

    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    val context =
        LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    LaunchedEffect(
        missionId
    ) {

        viewModel.load(
            missionId
        )

        viewModel.onEvent(
            NewEncounterEvent
                .LocationPermissionChanged(
                    hasLocationPermission(
                        context
                    )
                )
        )
    }

    DisposableEffect(
        lifecycleOwner,
        context
    ) {

        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {

                    viewModel.onEvent(
                        NewEncounterEvent
                            .LocationPermissionChanged(
                                hasLocationPermission(
                                    context
                                )
                            )
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

    LaunchedEffect(
        viewModel
    ) {

        viewModel
            .effects
            .collect { effect ->

                when (
                    effect
                ) {

                    is NewEncounterEffect
                    .EncounterSaved -> {
                        onEncounterSaved(
                            effect
                        )
                    }

                    is NewEncounterEffect
                    .ValidationFailed -> {
                        onValidationFailed(
                            effect.error
                        )
                    }

                    NewEncounterEffect
                        .LoadFailed -> {
                        onLoadFailed()
                    }

                    NewEncounterEffect
                        .SaveFailed -> {
                        onSaveFailed()
                    }
                }
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

    NewEncounterContent(
        uiState =
            uiState,

        onEvent =
            viewModel::onEvent,

        onBackClick =
            onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewEncounterContent(
    uiState: NewEncounterUiState,
    onEvent: (NewEncounterEvent) -> Unit,
    onBackClick: () -> Unit
) {

    var ageMenuExpanded by
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
                            .new_encounter_back
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
                                .new_encounter_title
                        )
                )

                if (
                    uiState
                        .missionName
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
        // Pessoa
        // =====================================================

        EncounterSectionCard(
            title =
                stringResource(
                    R.string
                        .new_encounter_person_section
                )
        ) {

            OutlinedTextField(
                value =
                    uiState.personName,
                onValueChange = {
                    onEvent(
                        NewEncounterEvent
                            .PersonNameChanged(
                                it
                            )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine =
                    true,
                label = {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .new_encounter_person_name
                            )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Person,
                        contentDescription =
                            null
                    )
                },
                shape =
                    RoundedCornerShape(
                        16.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            ExposedDropdownMenuBox(
                expanded =
                    ageMenuExpanded,
                onExpandedChange = {
                    ageMenuExpanded =
                        !ageMenuExpanded
                }
            ) {

                OutlinedTextField(
                    value =
                        uiState
                            .ageGroup
                            ?.let {
                                stringResource(
                                    ageGroupLabelRes(
                                        it
                                    )
                                )
                            }
                            .orEmpty(),
                    onValueChange = {},
                    readOnly =
                        true,
                    modifier =
                        Modifier
                            .menuAnchor(
                                type =
                                    ExposedDropdownMenuAnchorType
                                        .PrimaryNotEditable,
                                enabled =
                                    true
                            )
                            .fillMaxWidth(),
                    label = {
                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .new_encounter_age_group
                                )
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults
                            .TrailingIcon(
                                expanded =
                                    ageMenuExpanded
                            )
                    },
                    shape =
                        RoundedCornerShape(
                            16.dp
                        )
                )

                ExposedDropdownMenu(
                    expanded =
                        ageMenuExpanded,
                    onDismissRequest = {
                        ageMenuExpanded =
                            false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text(
                                text =
                                    stringResource(
                                        R.string
                                            .new_encounter_age_not_informed
                                    )
                            )
                        },
                        onClick = {

                            onEvent(
                                NewEncounterEvent
                                    .AgeGroupChanged(
                                        null
                                    )
                            )

                            ageMenuExpanded =
                                false
                        }
                    )

                    EncounterAgeGroup
                        .entries
                        .forEach { ageGroup ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text =
                                            stringResource(
                                                ageGroupLabelRes(
                                                    ageGroup
                                                )
                                            )
                                    )
                                },
                                onClick = {

                                    onEvent(
                                        NewEncounterEvent
                                            .AgeGroupChanged(
                                                ageGroup
                                            )
                                    )

                                    ageMenuExpanded =
                                        false
                                }
                            )
                        }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            val phoneInvalid =
                uiState
                    .phoneDigits
                    .isNotBlank() &&
                        !uiState
                            .hasValidPhone

            OutlinedTextField(
                value =
                    uiState.phoneDigits,
                onValueChange = {
                    onEvent(
                        NewEncounterEvent
                            .PhoneChanged(
                                it
                            )
                    )
                },
                visualTransformation =
                    BrazilianPhoneVisualTransformation,
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine =
                    true,
                label = {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .new_encounter_phone
                            )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Phone,
                        contentDescription =
                            null
                    )
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Phone
                    ),
                isError =
                    phoneInvalid,
                supportingText =
                    if (
                        phoneInvalid
                    ) {
                        {
                            Text(
                                text =
                                    stringResource(
                                        R.string
                                            .new_encounter_phone_invalid
                                    )
                            )
                        }
                    } else {
                        null
                    },
                shape =
                    RoundedCornerShape(
                        16.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            OutlinedTextField(
                value =
                    uiState.address,
                onValueChange = {
                    onEvent(
                        NewEncounterEvent
                            .AddressChanged(
                                it
                            )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .new_encounter_address
                            )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Home,
                        contentDescription =
                            null
                    )
                },
                minLines =
                    2,
                maxLines =
                    4,
                shape =
                    RoundedCornerShape(
                        16.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            EncounterSwitchRow(
                title =
                    stringResource(
                        R.string
                            .new_encounter_at_person_home
                    ),
                subtitle =
                    stringResource(
                        R.string
                            .new_encounter_at_person_home_help
                    ),
                checked =
                    uiState.isAtPersonHome,
                enabled =
                    !uiState.isResolvingAddress,
                onCheckedChange = {
                    onEvent(
                        NewEncounterEvent
                            .PersonHomeChanged(
                                it
                            )
                    )
                }
            )

            if (
                uiState.isResolvingAddress
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                18.dp
                            ),
                        strokeWidth =
                            2.dp
                    )

                    Text(
                        text =
                            stringResource(
                                R.string
                                    .new_encounter_resolving_address
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
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        // =====================================================
        // Localização
        // =====================================================

        EncounterLocationCard(
            uiState =
                uiState,
            onRefresh = {
                onEvent(
                    NewEncounterEvent
                        .RefreshLocation
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        // =====================================================
        // Ações
        // =====================================================

        EncounterSectionCard(
            title =
                stringResource(
                    R.string
                        .new_encounter_actions_section
                )
        ) {

            /*
             * As ações simples ficam agrupadas no topo.
             * Assim VISIT, PRAYER e OTHER permanecem juntas,
             * independentemente da ordem em que as atividades
             * foram configuradas na missão.
             */

            if (
                MissionActivityType.VISIT in
                uiState.availableActivities
            ) {

                SimpleActivityToggle(
                    title =
                        stringResource(
                            R.string
                                .new_encounter_activity_visit
                        ),
                    checked =
                        MissionActivityType
                            .VISIT in
                                uiState
                                    .performedActivities,
                    onCheckedChange = {
                        onEvent(
                            NewEncounterEvent
                                .ActivityToggled(
                                    MissionActivityType
                                        .VISIT
                                )
                        )
                    }
                )
            }

            if (
                MissionActivityType.PRAYER in
                uiState.availableActivities
            ) {

                SimpleActivityToggle(
                    title =
                        stringResource(
                            R.string
                                .new_encounter_activity_prayer
                        ),
                    checked =
                        MissionActivityType
                            .PRAYER in
                                uiState
                                    .performedActivities,
                    onCheckedChange = {
                        onEvent(
                            NewEncounterEvent
                                .ActivityToggled(
                                    MissionActivityType
                                        .PRAYER
                                )
                        )
                    }
                )
            }

            if (
                MissionActivityType.OTHER in
                uiState.availableActivities
            ) {

                SimpleActivityToggle(
                    title =
                        customActivityPerformedLabel(
                            customActivityName =
                                uiState
                                    .customActivityName
                        ),
                    checked =
                        MissionActivityType
                            .OTHER in
                                uiState
                                    .performedActivities,
                    onCheckedChange = {
                        onEvent(
                            NewEncounterEvent
                                .ActivityToggled(
                                    MissionActivityType
                                        .OTHER
                                )
                        )
                    }
                )
            }

            /*
             * Atividades que possuem conteúdo próprio vêm
             * depois das ações simples.
             */

            if (
                MissionActivityType.BIBLE_STUDY in
                uiState.availableActivities
            ) {

                BibleStudySection(
                    selectedStatus =
                        uiState
                            .bibleStudyStatus,
                    onStatusSelected = {
                        onEvent(
                            NewEncounterEvent
                                .BibleStudyStatusChanged(
                                    it
                                )
                        )
                    }
                )
            }

            if (
                MissionActivityType.MATERIAL_DELIVERY in
                uiState.availableActivities
            ) {

                MaterialsSection(
                    materials =
                        uiState
                            .availableMaterials,
                    customMaterialName =
                        uiState
                            .customMaterialName,
                    quantities =
                        uiState
                            .materialQuantities,
                    onQuantityChanged = {
                            material,
                            quantity ->

                        onEvent(
                            NewEncounterEvent
                                .MaterialQuantityChanged(
                                    material =
                                        material,
                                    quantity =
                                        quantity
                                )
                        )
                    }
                )
            }

            if (
                MissionActivityType.OPINION_SURVEY in
                uiState.availableActivities
            ) {

                SurveySection(
                    questions =
                        uiState
                            .surveyQuestions,
                    answers =
                        uiState
                            .surveyAnswers,
                    onAnswerChanged = {
                            questionId,
                            answer ->

                        onEvent(
                            NewEncounterEvent
                                .SurveyAnswerChanged(
                                    questionId =
                                        questionId,
                                    answer =
                                        answer
                                )
                        )
                    }
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        // =====================================================
        // Acompanhamento
        // =====================================================

        EncounterSectionCard(
            title =
                stringResource(
                    R.string
                        .new_encounter_follow_up_section
                )
        ) {

            EncounterSwitchRow(
                title =
                    stringResource(
                        R.string
                            .new_encounter_follow_up_accepted
                    ),
                subtitle =
                    stringResource(
                        R.string
                            .new_encounter_follow_up_help
                    ),
                checked =
                    uiState.acceptedFollowUp,
                onCheckedChange = {
                    onEvent(
                        NewEncounterEvent
                            .AcceptedFollowUpChanged(
                                it
                            )
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            OutlinedTextField(
                value =
                    uiState.notes,
                onValueChange = {
                    onEvent(
                        NewEncounterEvent
                            .NotesChanged(
                                it
                            )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .new_encounter_notes
                            )
                    )
                },
                minLines =
                    3,
                maxLines =
                    6,
                shape =
                    RoundedCornerShape(
                        16.dp
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        // =====================================================
        // Entrar em contato agora
        // =====================================================

        ContactNowCard(
            enabled =
                uiState.canContactNow,
            checked =
                uiState.contactNow,
            onCheckedChange = {
                onEvent(
                    NewEncounterEvent
                        .ContactNowChanged(
                            it
                        )
                )
            }
        )

        uiState
            .errorMessageRes
            ?.let { errorRes ->

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            errorRes
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .error
                )
            }

        Spacer(
            modifier =
                Modifier.height(
                    24.dp
                )
        )

        IdePrimaryButton(
            text =
                stringResource(
                    R.string
                        .new_encounter_save
                ),
            onClick = {
                onEvent(
                    NewEncounterEvent
                        .SaveClicked
                )
            },
            isLoading =
                uiState.isSaving,
            enabled =
                !uiState.isSaving,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(
                    24.dp
                )
        )
    }
}

@Composable
private fun EncounterSectionCard(
    title: String,
    content: @Composable () -> Unit
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                ),
        elevation =
            CardDefaults
                .cardElevation(
                    defaultElevation =
                        2.dp
                )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        18.dp
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
                        .onSurface
            )

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            content()
        }
    }
}

@Composable
private fun EncounterSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled =
                        enabled
                ) {
                    onCheckedChange(
                        !checked
                    )
                }
                .padding(
                    vertical = 4.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {

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
                    FontWeight.Medium
            )

            subtitle
                ?.let {

                    Spacer(
                        modifier =
                            Modifier.height(
                                2.dp
                            )
                    )

                    Text(
                        text =
                            it,
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

        Switch(
            checked =
                checked,
            onCheckedChange =
                onCheckedChange,
            enabled =
                enabled
        )
    }
}

@Composable
private fun EncounterLocationCard(
    uiState: NewEncounterUiState,
    onRefresh: () -> Unit
) {

    val icon =
        when {

            !uiState
                .hasLocationPermission ->
                Icons.Outlined.LocationOff

            uiState
                .hasLocation ->
                Icons.Outlined.CheckCircle

            else ->
                Icons.Outlined.LocationOn
        }

    val title =
        when {

            !uiState
                .hasLocationPermission ->
                stringResource(
                    R.string
                        .new_encounter_location_permission_missing
                )

            uiState
                .hasLocation ->
                stringResource(
                    R.string
                        .new_encounter_location_captured
                )

            uiState
                .isLoadingLocation ->
                stringResource(
                    R.string
                        .new_encounter_location_waiting
                )

            else ->
                stringResource(
                    R.string
                        .new_encounter_location_unavailable
                )
        }

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
                modifier =
                    Modifier.size(
                        46.dp
                    ),
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    if (
                        uiState.isLoadingLocation
                    ) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    22.dp
                                ),
                            strokeWidth =
                                2.dp
                        )

                    } else {

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
                    }
                }
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
                                .new_encounter_location_section
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

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
            }

            if (
                uiState.hasLocationPermission
            ) {

                IconButton(
                    onClick =
                        onRefresh
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Refresh,
                        contentDescription =
                            stringResource(
                                R.string
                                    .new_encounter_location_refresh
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun customActivityPerformedLabel(
    customActivityName: String?
): String {

    val genericOtherActivityLabel =
        stringResource(
            R.string
                .new_encounter_activity_other
        )

    val customName =
        customActivityName
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: return genericOtherActivityLabel

    val performedSuffix =
        genericOtherActivityLabel
            .substringAfterLast(
                " ",
                missingDelimiterValue =
                    ""
            )
            .trim()

    return if (
        performedSuffix.isNotBlank()
    ) {
        "$customName $performedSuffix"
    } else {
        customName
    }
}

@Composable
private fun SimpleActivityToggle(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onCheckedChange(
                        !checked
                    )
                }
                .padding(
                    vertical = 5.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Checkbox(
            checked =
                checked,
            onCheckedChange =
                onCheckedChange
        )

        Text(
            text =
                title,
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}

@Composable
private fun BibleStudySection(
    selectedStatus: BibleStudyStatus,
    onStatusSelected: (
        BibleStudyStatus
    ) -> Unit
) {

    EncounterSubsectionTitle(
        text =
            stringResource(
                R.string
                    .new_encounter_bible_study_title
            )
    )

    BibleStudyStatus
        .entries
        .forEach { status ->

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onStatusSelected(
                                status
                            )
                        },
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                RadioButton(
                    selected =
                        selectedStatus ==
                                status,
                    onClick = {
                        onStatusSelected(
                            status
                        )
                    }
                )

                Text(
                    text =
                        stringResource(
                            bibleStudyStatusLabelRes(
                                status
                            )
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )
            }
        }
}

@Composable
private fun MaterialsSection(
    materials: List<MissionMaterialType>,
    customMaterialName: String?,
    quantities: Map<MissionMaterialType, Int>,
    onQuantityChanged: (
        MissionMaterialType,
        Int
    ) -> Unit
) {

    EncounterSubsectionTitle(
        text =
            stringResource(
                R.string
                    .new_encounter_materials_title
            )
    )

    materials
        .forEach { material ->

            val quantity =
                quantities[
                    material
                ]
                    ?: 0

            MaterialQuantityRow(
                title =
                    if (
                        material ==
                        MissionMaterialType.OTHER
                    ) {
                        customMaterialName
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: stringResource(
                                R.string
                                    .new_encounter_material_other
                            )
                    } else {
                        stringResource(
                            materialLabelRes(
                                material
                            )
                        )
                    },
                quantity =
                    quantity,
                onDecrease = {
                    onQuantityChanged(
                        material,
                        (
                                quantity -
                                        1
                                )
                            .coerceAtLeast(
                                0
                            )
                    )
                },
                onIncrease = {
                    onQuantityChanged(
                        material,
                        quantity +
                                1
                    )
                }
            )
        }
}

@Composable
private fun MaterialQuantityRow(
    title: String,
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 6.dp
                ),
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
                    .bodyLarge,
            maxLines =
                2,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.weight(
                    1f
                )
        )

        IconButton(
            onClick =
                onDecrease,
            enabled =
                quantity > 0
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Remove,
                contentDescription =
                    stringResource(
                        R.string
                            .new_encounter_quantity_decrease
                    )
            )
        }

        Surface(
            shape =
                RoundedCornerShape(
                    10.dp
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        ) {

            Text(
                text =
                    quantity
                        .toString(),
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold,
                modifier =
                    Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    )
            )
        }

        IconButton(
            onClick =
                onIncrease
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.Add,
                contentDescription =
                    stringResource(
                        R.string
                            .new_encounter_quantity_increase
                    )
            )
        }
    }
}

@Composable
private fun SurveySection(
    questions: List<MissionSurveyQuestion>,
    answers: Map<String, String>,
    onAnswerChanged: (
        String,
        String
    ) -> Unit
) {

    EncounterSubsectionTitle(
        text =
            stringResource(
                R.string
                    .new_encounter_survey_title
            )
    )

    questions
        .forEachIndexed {
                index,
                question ->

            if (
                index > 0
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )
            }

            Text(
                text =
                    question.question,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
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
                        8.dp
                    )
            )

            when (
                question.type
            ) {

                SurveyQuestionType.TEXT -> {

                    OutlinedTextField(
                        value =
                            answers[
                                question.id
                            ]
                                .orEmpty(),
                        onValueChange = {
                            onAnswerChanged(
                                question.id,
                                it
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        minLines =
                            2,
                        maxLines =
                            4,
                        label = {
                            Text(
                                text =
                                    stringResource(
                                        R.string
                                            .new_encounter_survey_answer
                                    )
                            )
                        },
                        shape =
                            RoundedCornerShape(
                                16.dp
                            )
                    )
                }

                SurveyQuestionType.SINGLE_CHOICE -> {

                    question
                        .options
                        .filter {
                            it.isNotBlank()
                        }
                        .forEach { option ->

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAnswerChanged(
                                                question.id,
                                                option
                                            )
                                        },
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                RadioButton(
                                    selected =
                                        answers[
                                            question.id
                                        ] ==
                                                option,
                                    onClick = {
                                        onAnswerChanged(
                                            question.id,
                                            option
                                        )
                                    }
                                )

                                Text(
                                    text =
                                        option,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyLarge,
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                )
                            }
                        }
                }
            }
        }
}

@Composable
private fun EncounterSubsectionTitle(
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
                .primary,
        modifier =
            Modifier.padding(
                top = 6.dp,
                bottom = 6.dp
            )
    )
}

@Composable
private fun ContactNowCard(
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    val borderColor =
        if (
            checked &&
            enabled
        ) {
            MaterialTheme
                .colorScheme
                .primary
        } else {
            MaterialTheme
                .colorScheme
                .outlineVariant
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        border =
            BorderStroke(
                width =
                    1.dp,
                color =
                    borderColor
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled =
                            enabled
                    ) {
                        onCheckedChange(
                            !checked
                        )
                    }
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
                modifier =
                    Modifier.size(
                        46.dp
                    ),
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Phone,
                        contentDescription =
                            null,
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
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
                                .new_encounter_contact_now
                        ),
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
                            2.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            if (
                                enabled
                            ) {
                                R.string
                                    .new_encounter_contact_now_help
                            } else {
                                R.string
                                    .new_encounter_contact_now_requires_phone
                            }
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

            Switch(
                checked =
                    checked,
                onCheckedChange =
                    onCheckedChange,
                enabled =
                    enabled
            )
        }
    }
}

private fun hasLocationPermission(
    context: Context
): Boolean {

    val fineGranted =
        ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission
                    .ACCESS_FINE_LOCATION
            ) ==
                PackageManager
                    .PERMISSION_GRANTED

    val coarseGranted =
        ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission
                    .ACCESS_COARSE_LOCATION
            ) ==
                PackageManager
                    .PERMISSION_GRANTED

    return fineGranted ||
            coarseGranted
}

private object BrazilianPhoneVisualTransformation :
    VisualTransformation {

    override fun filter(
        text: AnnotatedString
    ): TransformedText {

        val digits =
            text.text
                .filter(Char::isDigit)
                .take(11)

        val formatted =
            buildString {

                if (digits.isNotEmpty()) {
                    append("(")
                    append(digits[0])
                }

                if (digits.length >= 2) {
                    append(digits[1])
                }

                if (digits.length >= 3) {
                    append(") ")
                    append(digits[2])
                }

                if (digits.length >= 4) {
                    append(" ")
                    append(
                        digits.substring(
                            3,
                            minOf(
                                7,
                                digits.length
                            )
                        )
                    )
                }

                if (digits.length >= 8) {
                    append("-")
                    append(
                        digits.substring(
                            7
                        )
                    )
                }
            }

        val originalLength =
            digits.length

        val transformedLength =
            formatted.length

        val offsetMapping =
            object : OffsetMapping {

                override fun originalToTransformed(
                    offset: Int
                ): Int {

                    val safeOffset =
                        offset.coerceIn(
                            0,
                            originalLength
                        )

                    val mappedOffset =
                        when (safeOffset) {
                            0 -> 0
                            1 -> 2
                            2 -> 3
                            3 -> 6
                            4 -> 8
                            5 -> 9
                            6 -> 10
                            7 -> 11
                            8 -> 13
                            9 -> 14
                            10 -> 15
                            else -> 16
                        }

                    return mappedOffset
                        .coerceAtMost(
                            transformedLength
                        )
                }

                override fun transformedToOriginal(
                    offset: Int
                ): Int {

                    val safeOffset =
                        offset.coerceIn(
                            0,
                            transformedLength
                        )

                    val mappedOffset =
                        when {
                            safeOffset <= 1 -> 0
                            safeOffset <= 2 -> 1
                            safeOffset <= 5 -> 2
                            safeOffset <= 7 -> 3
                            safeOffset <= 8 -> 4
                            safeOffset <= 9 -> 5
                            safeOffset <= 10 -> 6
                            safeOffset <= 12 -> 7
                            safeOffset <= 13 -> 8
                            safeOffset <= 14 -> 9
                            safeOffset <= 15 -> 10
                            else -> 11
                        }

                    return mappedOffset
                        .coerceAtMost(
                            originalLength
                        )
                }
            }

        return TransformedText(
            text =
                AnnotatedString(
                    formatted
                ),
            offsetMapping =
                offsetMapping
        )
    }
}

private fun ageGroupLabelRes(
    ageGroup: EncounterAgeGroup
): Int {

    return when (
        ageGroup
    ) {

        EncounterAgeGroup.CHILD ->
            R.string
                .new_encounter_age_child

        EncounterAgeGroup.TEENAGER ->
            R.string
                .new_encounter_age_teenager

        EncounterAgeGroup.YOUTH ->
            R.string
                .new_encounter_age_youth

        EncounterAgeGroup.ADULT ->
            R.string
                .new_encounter_age_adult

        EncounterAgeGroup.ELDERLY ->
            R.string
                .new_encounter_age_elderly
    }
}

private fun bibleStudyStatusLabelRes(
    status: BibleStudyStatus
): Int {

    return when (
        status
    ) {

        BibleStudyStatus.NOT_OFFERED ->
            R.string
                .new_encounter_bible_study_not_offered

        BibleStudyStatus.OFFERED ->
            R.string
                .new_encounter_bible_study_offered

        BibleStudyStatus.ACCEPTED ->
            R.string
                .new_encounter_bible_study_accepted
    }
}

private fun materialLabelRes(
    material: MissionMaterialType
): Int {

    return when (
        material
    ) {

        MissionMaterialType.BOOK ->
            R.string
                .new_encounter_material_book

        MissionMaterialType.MAGAZINE ->
            R.string
                .new_encounter_material_magazine

        MissionMaterialType.LEAFLET ->
            R.string
                .new_encounter_material_leaflet

        MissionMaterialType.OTHER ->
            R.string
                .new_encounter_material_other
    }
}
