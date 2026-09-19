package br.com.ide.presentation.feature.newencounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionEncounterMaterial
import br.com.ide.domain.model.MissionEncounterSurveyAnswer
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.usecase.CreateMissionEncounterUseCase
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import br.com.ide.domain.usecase.ObserveMissionParticipantsUseCase
import br.com.ide.domain.usecase.ReverseGeocodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NewEncounterViewModel @Inject constructor(

    private val getMissionByIdUseCase:
    GetMissionByIdUseCase,

    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,

    private val observeMissionParticipantsUseCase:
    ObserveMissionParticipantsUseCase,

    private val reverseGeocodeUseCase:
    ReverseGeocodeUseCase,

    private val createMissionEncounterUseCase:
    CreateMissionEncounterUseCase

) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            NewEncounterUiState()
        )

    val uiState:
            StateFlow<NewEncounterUiState> =
        _uiState.asStateFlow()

    private val _effects =
        MutableSharedFlow<NewEncounterEffect>()

    val effects:
            SharedFlow<NewEncounterEffect> =
        _effects.asSharedFlow()

    private var loadedMissionId:
            String? =
        null

    private var loadedMission:
            Mission? =
        null

    private var currentUserProfile:
            UserProfile? =
        null

    private var currentParticipant:
            MissionParticipantState? =
        null

    private var participantsJob:
            Job? =
        null

    /*
     * A localização do participante já é atualizada pelo
     * serviço em foreground durante a missão.
     *
     * O Novo encontro observa o mesmo documento do participante.
     * Assim não criamos um segundo rastreador de GPS concorrente.
     */
    private var latestParticipantLatitude:
            Double? =
        null

    private var latestParticipantLongitude:
            Double? =
        null

    /*
     * Evita geocodificar novamente a cada atualização periódica
     * do GPS quando o endereço já foi preenchido automaticamente.
     */
    private var automaticHomeAddressResolved:
            Boolean =
        false

    // =========================================================
    // Carregamento
    // =========================================================

    fun load(
        missionId: String
    ) {

        if (
            loadedMissionId ==
            missionId
        ) {
            return
        }

        stopParticipantsObservation()

        loadedMissionId =
            missionId

        loadedMission =
            null

        currentUserProfile =
            null

        currentParticipant =
            null

        latestParticipantLatitude =
            null

        latestParticipantLongitude =
            null

        automaticHomeAddressResolved =
            false

        _uiState.value =
            NewEncounterUiState(
                missionId =
                    missionId,
                isLoading =
                    true
            )

        viewModelScope.launch {

            try {

                val mission =
                    getMissionByIdUseCase(
                        missionId
                    )

                if (
                    mission ==
                    null
                ) {

                    _uiState.update {
                        it.copy(
                            isLoading =
                                false
                        )
                    }

                    _effects.emit(
                        NewEncounterEffect
                            .LoadFailed
                    )

                    return@launch
                }

                val userProfile =
                    getCurrentUserProfileUseCase()
                        .getOrNull()

                if (
                    userProfile ==
                    null
                ) {

                    _uiState.update {
                        it.copy(
                            missionId =
                                mission.id,
                            missionName =
                                mission.name,
                            isLoading =
                                false
                        )
                    }

                    _effects.emit(
                        NewEncounterEffect
                            .LoadFailed
                    )

                    return@launch
                }

                loadedMission =
                    mission

                currentUserProfile =
                    userProfile

                val initialMaterialQuantities =
                    mission
                        .materials
                        .associateWith {
                            0
                        }

                val initialSurveyAnswers =
                    mission
                        .surveyQuestions
                        .associate {
                            it.id to ""
                        }

                _uiState.update {
                    it.copy(
                        missionId =
                            mission.id,

                        missionName =
                            mission.name,

                        availableActivities =
                            mission.activities,

                        customActivityName =
                            mission.customActivityName,

                        availableMaterials =
                            mission.materials,

                        customMaterialName =
                            mission.customMaterialName,

                        surveyQuestions =
                            mission.surveyQuestions,

                        materialQuantities =
                            initialMaterialQuantities,

                        surveyAnswers =
                            initialSurveyAnswers,

                        isLoading =
                            false,

                        errorMessageRes =
                            null
                    )
                }

                startParticipantsObservation(
                    missionId =
                        mission.id,
                    currentUserId =
                        userProfile.id
                )

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        isLoading =
                            false
                    )
                }

                _effects.emit(
                    NewEncounterEffect
                        .LoadFailed
                )
            }
        }
    }

    // =========================================================
    // Eventos
    // =========================================================

    fun onEvent(
        event: NewEncounterEvent
    ) {

        when (
            event
        ) {

            is NewEncounterEvent
            .PersonNameChanged -> {

                _uiState.update {
                    it.copy(
                        personName =
                            event.value
                    )
                }
            }

            is NewEncounterEvent
            .AgeGroupChanged -> {

                _uiState.update {
                    it.copy(
                        ageGroup =
                            event.value
                    )
                }
            }

            is NewEncounterEvent
            .PhoneChanged -> {

                val normalizedPhone =
                    normalizeBrazilianPhoneDigits(
                        event.value
                    )

                _uiState.update { state ->
                    state.copy(
                        phoneDigits =
                            normalizedPhone,

                        /*
                         * A mensagem textual será associada
                         * aos resources quando chegarmos ao
                         * passo das strings.
                         */
                        phoneErrorMessageRes =
                            null,

                        contactNow =
                            if (
                                normalizedPhone
                                    .length ==
                                BRAZILIAN_MOBILE_LENGTH
                            ) {
                                state.contactNow
                            } else {
                                false
                            }
                    )
                }
            }

            is NewEncounterEvent
            .AddressChanged -> {

                automaticHomeAddressResolved =
                    event.value
                        .isNotBlank()

                _uiState.update {
                    it.copy(
                        address =
                            event.value
                    )
                }
            }

            is NewEncounterEvent
            .LocationPermissionChanged -> {

                onLocationPermissionChanged(
                    granted =
                        event.granted
                )
            }

            NewEncounterEvent
                .RefreshLocation -> {

                applyLatestParticipantLocation()
            }

            is NewEncounterEvent
            .PersonHomeChanged -> {

                onPersonHomeChanged(
                    isAtPersonHome =
                        event.value
                )
            }

            is NewEncounterEvent
            .ActivityToggled -> {

                toggleActivity(
                    activity =
                        event.activity
                )
            }

            is NewEncounterEvent
            .BibleStudyStatusChanged -> {

                updateBibleStudyStatus(
                    status =
                        event.status
                )
            }

            is NewEncounterEvent
            .MaterialQuantityChanged -> {

                updateMaterialQuantity(
                    material =
                        event.material,
                    quantity =
                        event.quantity
                )
            }

            is NewEncounterEvent
            .SurveyAnswerChanged -> {

                updateSurveyAnswer(
                    questionId =
                        event.questionId,
                    answer =
                        event.answer
                )
            }

            is NewEncounterEvent
            .AcceptedFollowUpChanged -> {

                _uiState.update {
                    it.copy(
                        acceptedFollowUp =
                            event.value
                    )
                }
            }

            is NewEncounterEvent
            .NotesChanged -> {

                _uiState.update {
                    it.copy(
                        notes =
                            event.value
                    )
                }
            }

            is NewEncounterEvent
            .ContactNowChanged -> {

                _uiState.update { state ->
                    state.copy(
                        contactNow =
                            event.value &&
                                    state.hasValidPhone
                    )
                }
            }

            NewEncounterEvent
                .SaveClicked -> {

                saveEncounter()
            }
        }
    }

    // =========================================================
    // Participante atual / localização
    // =========================================================

    private fun startParticipantsObservation(
        missionId: String,
        currentUserId: String
    ) {

        stopParticipantsObservation()

        participantsJob =
            viewModelScope.launch {

                observeMissionParticipantsUseCase(
                    missionId =
                        missionId
                )
                    .catch {
                        /*
                         * Falha de localização não impede
                         * o preenchimento do encontro.
                         *
                         * O encontro poderá ser salvo sem
                         * coordenadas.
                         */
                    }
                    .collect { participants ->

                        val participant =
                            participants
                                .firstOrNull {
                                    it.userId ==
                                            currentUserId
                                }

                        currentParticipant =
                            participant

                        latestParticipantLatitude =
                            participant
                                ?.latitude

                        latestParticipantLongitude =
                            participant
                                ?.longitude

                        if (
                            _uiState.value
                                .hasLocationPermission
                        ) {
                            applyLatestParticipantLocation()
                        }
                    }
            }
    }

    private fun stopParticipantsObservation() {

        participantsJob
            ?.cancel()

        participantsJob =
            null
    }

    private fun onLocationPermissionChanged(
        granted: Boolean
    ) {

        _uiState.update { state ->

            state.copy(
                hasLocationPermission =
                    granted,

                latitude =
                    if (
                        granted
                    ) {
                        latestParticipantLatitude
                    } else {
                        null
                    },

                longitude =
                    if (
                        granted
                    ) {
                        latestParticipantLongitude
                    } else {
                        null
                    },

                isLoadingLocation =
                    granted &&
                            (
                                    latestParticipantLatitude ==
                                            null ||
                                            latestParticipantLongitude ==
                                            null
                                    )
            )
        }

        if (
            granted
        ) {
            maybeResolveHomeAddress()
        }
    }

    private fun applyLatestParticipantLocation() {

        val state =
            _uiState.value

        if (
            !state.hasLocationPermission
        ) {
            return
        }

        _uiState.update {
            it.copy(
                latitude =
                    latestParticipantLatitude,

                longitude =
                    latestParticipantLongitude,

                isLoadingLocation =
                    latestParticipantLatitude ==
                            null ||
                            latestParticipantLongitude ==
                            null
            )
        }

        maybeResolveHomeAddress()
    }

    // =========================================================
    // Casa da pessoa / endereço automático
    // =========================================================

    private fun onPersonHomeChanged(
        isAtPersonHome: Boolean
    ) {

        if (
            !isAtPersonHome
        ) {

            automaticHomeAddressResolved =
                false

            _uiState.update {
                it.copy(
                    isAtPersonHome =
                        false,
                    isResolvingAddress =
                        false
                )
            }

            return
        }

        _uiState.update {
            it.copy(
                isAtPersonHome =
                    true
            )
        }

        maybeResolveHomeAddress()
    }

    private fun maybeResolveHomeAddress() {

        val state =
            _uiState.value

        if (
            !state.isAtPersonHome ||
            !state.hasLocationPermission ||
            state.latitude ==
            null ||
            state.longitude ==
            null ||
            state.isResolvingAddress ||
            automaticHomeAddressResolved ||
            state.address.isNotBlank()
        ) {
            return
        }

        resolveHomeAddress(
            latitude =
                state.latitude,
            longitude =
                state.longitude
        )
    }

    private fun resolveHomeAddress(
        latitude: Double,
        longitude: Double
    ) {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isResolvingAddress =
                        true
                )
            }

            val geocodedAddress =
                reverseGeocodeUseCase(
                    latitude =
                        latitude,
                    longitude =
                        longitude
                )
                    .getOrNull()

            val address =
                geocodedAddress
                    ?.address
                    ?.trim()
                    .orEmpty()

            automaticHomeAddressResolved =
                address.isNotBlank()

            _uiState.update { state ->
                state.copy(
                    address =
                        if (
                            state.address.isBlank() &&
                            state.isAtPersonHome
                        ) {
                            address
                        } else {
                            state.address
                        },

                    isResolvingAddress =
                        false
                )
            }
        }
    }

    // =========================================================
    // Ações realizadas
    // =========================================================

    private fun toggleActivity(
        activity: MissionActivityType
    ) {

        val state =
            _uiState.value

        if (
            activity !in
            state.availableActivities
        ) {
            return
        }

        /*
         * Estes três tipos possuem campos próprios.
         * O estado de "realizada" é derivado desses campos,
         * evitando dados contraditórios.
         */
        if (
            activity ==
            MissionActivityType.BIBLE_STUDY ||
            activity ==
            MissionActivityType.MATERIAL_DELIVERY ||
            activity ==
            MissionActivityType.OPINION_SURVEY
        ) {
            return
        }

        _uiState.update { currentState ->

            val updated =
                currentState
                    .performedActivities
                    .toMutableSet()

            if (
                activity in
                updated
            ) {
                updated.remove(
                    activity
                )
            } else {
                updated.add(
                    activity
                )
            }

            currentState.copy(
                performedActivities =
                    updated
            )
        }
    }

    private fun updateBibleStudyStatus(
        status: BibleStudyStatus
    ) {

        val state =
            _uiState.value

        if (
            MissionActivityType.BIBLE_STUDY !in
            state.availableActivities
        ) {
            return
        }

        _uiState.update { currentState ->

            val updatedActivities =
                currentState
                    .performedActivities
                    .toMutableSet()

            if (
                status ==
                BibleStudyStatus.NOT_OFFERED
            ) {
                updatedActivities.remove(
                    MissionActivityType
                        .BIBLE_STUDY
                )
            } else {
                updatedActivities.add(
                    MissionActivityType
                        .BIBLE_STUDY
                )
            }

            currentState.copy(
                bibleStudyStatus =
                    status,

                performedActivities =
                    updatedActivities
            )
        }
    }

    private fun updateMaterialQuantity(
        material: MissionMaterialType,
        quantity: Int
    ) {

        val state =
            _uiState.value

        if (
            material !in
            state.availableMaterials
        ) {
            return
        }

        _uiState.update { currentState ->

            val quantities =
                currentState
                    .materialQuantities
                    .toMutableMap()

            quantities[
                material
            ] =
                quantity.coerceAtLeast(
                    0
                )

            val hasDeliveredMaterial =
                quantities
                    .values
                    .any {
                        it > 0
                    }

            val updatedActivities =
                currentState
                    .performedActivities
                    .toMutableSet()

            if (
                hasDeliveredMaterial
            ) {
                updatedActivities.add(
                    MissionActivityType
                        .MATERIAL_DELIVERY
                )
            } else {
                updatedActivities.remove(
                    MissionActivityType
                        .MATERIAL_DELIVERY
                )
            }

            currentState.copy(
                materialQuantities =
                    quantities,

                performedActivities =
                    updatedActivities
            )
        }
    }

    private fun updateSurveyAnswer(
        questionId: String,
        answer: String
    ) {

        val state =
            _uiState.value

        if (
            MissionActivityType.OPINION_SURVEY !in
            state.availableActivities ||
            state
                .surveyQuestions
                .none {
                    it.id ==
                            questionId
                }
        ) {
            return
        }

        _uiState.update { currentState ->

            val answers =
                currentState
                    .surveyAnswers
                    .toMutableMap()

            answers[
                questionId
            ] =
                answer

            val surveyCompleted =
                currentState
                    .surveyQuestions
                    .isNotEmpty() &&
                        currentState
                            .surveyQuestions
                            .all { question ->
                                answers[
                                    question.id
                                ]
                                    ?.isNotBlank() ==
                                        true
                            }

            val updatedActivities =
                currentState
                    .performedActivities
                    .toMutableSet()

            if (
                surveyCompleted
            ) {
                updatedActivities.add(
                    MissionActivityType
                        .OPINION_SURVEY
                )
            } else {
                updatedActivities.remove(
                    MissionActivityType
                        .OPINION_SURVEY
                )
            }

            currentState.copy(
                surveyAnswers =
                    answers,

                performedActivities =
                    updatedActivities
            )
        }
    }

    // =========================================================
    // Salvamento
    // =========================================================

    private fun saveEncounter() {

        val state =
            _uiState.value

        val mission =
            loadedMission

        val userProfile =
            currentUserProfile

        if (
            state.isLoading ||
            state.isSaving ||
            mission ==
            null ||
            userProfile ==
            null
        ) {
            return
        }

        if (
            state.phoneDigits
                .isNotBlank() &&
            !state.hasValidPhone
        ) {

            viewModelScope.launch {
                _effects.emit(
                    NewEncounterEffect
                        .ValidationFailed(
                            NewEncounterValidationError
                                .INVALID_PHONE
                        )
                )
            }

            return
        }

        if (
            state.contactNow &&
            !state.hasValidPhone
        ) {

            viewModelScope.launch {
                _effects.emit(
                    NewEncounterEffect
                        .ValidationFailed(
                            NewEncounterValidationError
                                .INVALID_PHONE
                        )
                )
            }

            return
        }

        val currentGroupId =
            currentParticipant
                ?.groupId

        val currentGroupColorHex =
            currentGroupId
                ?.let { groupId ->
                    mission
                        .groups
                        .firstOrNull {
                            it.id ==
                                    groupId
                        }
                        ?.colorHex
                }

        val materials =
            state
                .availableMaterials
                .mapNotNull { material ->

                    val quantity =
                        state
                            .materialQuantities[
                            material
                        ]
                            ?: 0

                    if (
                        quantity <= 0
                    ) {
                        return@mapNotNull null
                    }

                    MissionEncounterMaterial(
                        type =
                            material,

                        customName =
                            if (
                                material ==
                                MissionMaterialType.OTHER
                            ) {
                                state.customMaterialName
                            } else {
                                null
                            },

                        quantity =
                            quantity
                    )
                }

        val surveyAnswers =
            state
                .surveyQuestions
                .mapNotNull { question ->

                    val answer =
                        state
                            .surveyAnswers[
                            question.id
                        ]
                            ?.trim()
                            .orEmpty()

                    if (
                        answer.isBlank()
                    ) {
                        return@mapNotNull null
                    }

                    MissionEncounterSurveyAnswer(
                        questionId =
                            question.id,

                        question =
                            question.question,

                        type =
                            question.type,

                        answer =
                            answer
                    )
                }

        val encounter =
            MissionEncounter(
                missionId =
                    mission.id,

                registeredByUserId =
                    userProfile.id,

                groupId =
                    currentGroupId,

                groupColorHex =
                    currentGroupColorHex,

                personName =
                    state
                        .personName
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        },

                ageGroup =
                    state.ageGroup,

                phoneDigits =
                    state
                        .phoneDigits
                        .takeIf {
                            it.isNotBlank()
                        },

                address =
                    state
                        .address
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        },

                isAtPersonHome =
                    state.isAtPersonHome,

                latitude =
                    state.latitude,

                longitude =
                    state.longitude,

                performedActivities =
                    state
                        .performedActivities
                        .filter {
                            it in
                                    state.availableActivities
                        },

                bibleStudyStatus =
                    state.bibleStudyStatus,

                materials =
                    materials,

                surveyAnswers =
                    surveyAnswers,

                acceptedFollowUp =
                    state.acceptedFollowUp,

                notes =
                    state
                        .notes
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
            )

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isSaving =
                        true
                )
            }

            createMissionEncounterUseCase(
                encounter
            )
                .onSuccess { encounterId ->

                    _uiState.update {
                        it.copy(
                            isSaving =
                                false
                        )
                    }

                    _effects.emit(
                        NewEncounterEffect
                            .EncounterSaved(
                                encounterId =
                                    encounterId,

                                contactNow =
                                    state.contactNow,

                                phoneDigits =
                                    state.phoneDigits,

                                personName =
                                    state
                                        .personName
                                        .trim()
                                        .takeIf {
                                            it.isNotBlank()
                                        }
                            )
                    )
                }
                .onFailure {

                    _uiState.update {
                        it.copy(
                            isSaving =
                                false
                        )
                    }

                    _effects.emit(
                        NewEncounterEffect
                            .SaveFailed
                    )
                }
        }
    }

    // =========================================================
    // Telefone
    // =========================================================

    private fun normalizeBrazilianPhoneDigits(
        value: String
    ): String {

        val digits =
            value.filter(
                Char::isDigit
            )

        return when {

            digits.length ==
                    BRAZILIAN_PHONE_WITH_COUNTRY_CODE_LENGTH &&
                    digits.startsWith(
                        BRAZIL_COUNTRY_CODE
                    ) -> {

                digits.drop(
                    BRAZIL_COUNTRY_CODE.length
                )
            }

            digits.length <=
                    BRAZILIAN_MOBILE_LENGTH -> {
                digits
            }

            else -> {
                digits.take(
                    BRAZILIAN_MOBILE_LENGTH
                )
            }
        }
    }

    override fun onCleared() {

        stopParticipantsObservation()

        super.onCleared()
    }

    private companion object {

        const val BRAZIL_COUNTRY_CODE =
            "55"

        const val BRAZILIAN_MOBILE_LENGTH =
            11

        const val BRAZILIAN_PHONE_WITH_COUNTRY_CODE_LENGTH =
            13
    }
}

sealed interface NewEncounterEffect {

    data class EncounterSaved(
        val encounterId: String,
        val contactNow: Boolean,
        val phoneDigits: String,
        val personName: String?
    ) : NewEncounterEffect

    data class ValidationFailed(
        val error: NewEncounterValidationError
    ) : NewEncounterEffect

    data object LoadFailed :
        NewEncounterEffect

    data object SaveFailed :
        NewEncounterEffect
}

enum class NewEncounterValidationError {
    INVALID_PHONE
}
