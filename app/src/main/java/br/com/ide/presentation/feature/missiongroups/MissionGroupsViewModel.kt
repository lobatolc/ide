package br.com.ide.presentation.feature.missiongroups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.MissionGroup
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import br.com.ide.domain.usecase.GetMissionGroupParticipantsUseCase
import br.com.ide.domain.usecase.UpdateMissionGroupsUseCase
import br.com.ide.domain.util.GroupColorGenerator
import br.com.ide.presentation.components.snackbar.IdeSnackbarManager
import br.com.ide.presentation.components.snackbar.IdeSnackbarMessage
import br.com.ide.presentation.components.snackbar.IdeSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MissionGroupsViewModel @Inject constructor(
    private val getMissionByIdUseCase:
    GetMissionByIdUseCase,

    private val getMissionGroupParticipantsUseCase:
    GetMissionGroupParticipantsUseCase,

    private val updateMissionGroupsUseCase:
    UpdateMissionGroupsUseCase,

    private val snackbarManager:
    IdeSnackbarManager
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MissionGroupsUiState()
        )

    val uiState:
            StateFlow<MissionGroupsUiState> =
        _uiState.asStateFlow()

    private var loadedMissionId:
            String? =
        null

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

        loadedMissionId =
            missionId

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {

                val mission =
                    getMissionByIdUseCase(
                        missionId
                    )

                if (
                    mission == null
                ) {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                R.string
                                    .mission_groups_loading_error
                        )
                    }

                    return@launch
                }

                val eligibleParticipants =
                    getMissionGroupParticipantsUseCase(
                        participatingChurchIds =
                            mission
                                .participatingChurchIds
                    )
                        .getOrThrow()

                val eligibleParticipantIds =
                    eligibleParticipants
                        .map {
                            it.id
                        }
                        .toSet()

                val sanitizedGroups =
                    sanitizeGroups(
                        groups =
                            mission.groups,

                        eligibleParticipantIds =
                            eligibleParticipantIds
                    )

                _uiState.update {
                    it.copy(
                        missionId =
                            mission.id,

                        missionName =
                            mission.name,

                        missionStatus =
                            mission.status,

                        eligibleParticipants =
                            eligibleParticipants,

                        groups =
                            sanitizedGroups,

                        isLoading =
                            false,

                        errorMessage =
                            null
                    )
                }

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        isLoading =
                            false,

                        errorMessage =
                            R.string
                                .mission_groups_loading_error
                    )
                }
            }
        }
    }

    // =========================================================
    // Eventos
    // =========================================================

    fun onEvent(
        event: MissionGroupsEvent
    ) {

        if (
            event !is MissionGroupsEvent.Save &&
            !_uiState.value.canEdit
        ) {
            return
        }

        when (
            event
        ) {

            is MissionGroupsEvent.CreateGroup -> {

                createGroup(
                    name =
                        event.name
                )
            }

            is MissionGroupsEvent.RenameGroup -> {

                renameGroup(
                    groupId =
                        event.groupId,

                    name =
                        event.name
                )
            }

            is MissionGroupsEvent.DeleteGroup -> {

                deleteGroup(
                    groupId =
                        event.groupId
                )
            }

            is MissionGroupsEvent.AddParticipant -> {

                addParticipant(
                    groupId =
                        event.groupId,

                    participantId =
                        event.participantId
                )
            }

            is MissionGroupsEvent.RemoveParticipant -> {

                removeParticipant(
                    groupId =
                        event.groupId,

                    participantId =
                        event.participantId
                )
            }

            is MissionGroupsEvent.SetSupport -> {

                setSupport(
                    groupId =
                        event.groupId,

                    participantId =
                        event.participantId
                )
            }

            MissionGroupsEvent.Save -> {

                save()
            }
        }
    }

    // =========================================================
    // Criar grupo
    // =========================================================

    private fun createGroup(
        name: String
    ) {

        val state =
            _uiState.value

        if (
            state.unassignedParticipants.isEmpty()
        ) {
            return
        }

        val normalizedName =
            name.trim()

        if (
            normalizedName.isBlank()
        ) {
            return
        }

        _uiState.update { state ->

            val existingColors =
                state.groups
                    .map {
                        it.colorHex
                    }

            val colorHex =
                GroupColorGenerator
                    .generate(
                        existingColors =
                            existingColors
                    )

            val newGroup =
                MissionGroup(
                    id =
                        UUID
                            .randomUUID()
                            .toString(),

                    name =
                        normalizedName,

                    colorHex =
                        colorHex,

                    participantIds =
                        emptyList(),

                    supportUserId =
                        null
                )

            state.copy(
                groups =
                    state.groups +
                            newGroup,

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Renomear grupo
    // =========================================================

    private fun renameGroup(
        groupId: String,
        name: String
    ) {

        val normalizedName =
            name.trim()

        if (
            normalizedName.isBlank()
        ) {
            return
        }

        _uiState.update { state ->

            state.copy(
                groups =
                    state.groups
                        .map { group ->

                            if (
                                group.id ==
                                groupId
                            ) {

                                group.copy(
                                    name =
                                        normalizedName
                                )

                            } else {

                                group
                            }
                        },

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Excluir grupo
    // =========================================================

    private fun deleteGroup(
        groupId: String
    ) {

        _uiState.update { state ->

            state.copy(
                groups =
                    state.groups
                        .filterNot {
                            it.id ==
                                    groupId
                        },

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Adicionar participante
    // =========================================================

    private fun addParticipant(
        groupId: String,
        participantId: String
    ) {

        val state =
            _uiState.value

        val participantExists =
            state
                .eligibleParticipants
                .any {
                    it.id ==
                            participantId
                }

        if (
            !participantExists
        ) {
            return
        }

        val groupExists =
            state.groups
                .any {
                    it.id ==
                            groupId
                }

        if (
            !groupExists
        ) {
            return
        }

        _uiState.update { currentState ->

            val updatedGroups =
                currentState.groups
                    .map { group ->

                        // Primeiro remove o usuário de qualquer
                        // outro grupo da missão.
                        val participantIdsWithoutUser =
                            group
                                .participantIds
                                .filterNot {
                                    it ==
                                            participantId
                                }

                        val supportUserId =
                            if (
                                group.supportUserId ==
                                participantId
                            ) {
                                null
                            } else {
                                group.supportUserId
                            }

                        if (
                            group.id ==
                            groupId
                        ) {

                            group.copy(
                                participantIds =
                                    (
                                            participantIdsWithoutUser +
                                                    participantId
                                            )
                                        .distinct(),

                                supportUserId =
                                    supportUserId
                            )

                        } else {

                            group.copy(
                                participantIds =
                                    participantIdsWithoutUser,

                                supportUserId =
                                    supportUserId
                            )
                        }
                    }

            currentState.copy(
                groups =
                    updatedGroups,

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Remover participante
    // =========================================================

    private fun removeParticipant(
        groupId: String,
        participantId: String
    ) {

        _uiState.update { state ->

            state.copy(
                groups =
                    state.groups
                        .map { group ->

                            if (
                                group.id ==
                                groupId
                            ) {

                                group.copy(
                                    participantIds =
                                        group
                                            .participantIds
                                            .filterNot {
                                                it ==
                                                        participantId
                                            },

                                    supportUserId =
                                        if (
                                            group.supportUserId ==
                                            participantId
                                        ) {
                                            null
                                        } else {
                                            group.supportUserId
                                        }
                                )

                            } else {

                                group
                            }
                        },

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Definir Apoio
    // =========================================================

    private fun setSupport(
        groupId: String,
        participantId: String?
    ) {

        _uiState.update { state ->

            state.copy(
                groups =
                    state.groups
                        .map { group ->

                            if (
                                group.id !=
                                groupId
                            ) {
                                return@map group
                            }

                            if (
                                participantId ==
                                null
                            ) {

                                return@map group.copy(
                                    supportUserId =
                                        null
                                )
                            }

                            val belongsToGroup =
                                participantId in
                                        group
                                            .participantIds

                            if (
                                !belongsToGroup
                            ) {

                                group

                            } else {

                                group.copy(
                                    supportUserId =
                                        participantId
                                )
                            }
                        },

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Salvar
    // =========================================================

    private fun save() {

        val state =
            _uiState.value

        if (
            state.isSaving ||
            !state.canEdit ||
            state.missionId.isBlank()
        ) {
            return
        }

        if (
            state.groups.any {
                it.participantIds.isEmpty()
            }
        ) {
            viewModelScope.launch {
                snackbarManager.show(
                    IdeSnackbarMessage(
                        messageRes =
                            R.string
                                .mission_groups_empty_group_save_error,
                        type =
                            IdeSnackbarType.WARNING
                    )
                )
            }

            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isSaving =
                        true,

                    errorMessage =
                        null
                )
            }

            updateMissionGroupsUseCase(
                missionId =
                    state.missionId,

                groups =
                    state.groups
            )
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            isSaving =
                                false,

                            isSaved =
                                true
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_groups_save_success,

                            type =
                                IdeSnackbarType.SUCCESS
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

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_groups_save_error,

                            type =
                                IdeSnackbarType.ERROR
                        )
                    )
                }
        }
    }

    // =========================================================
    // Sanitização
    // =========================================================

    private fun sanitizeGroups(
        groups: List<MissionGroup>,
        eligibleParticipantIds: Set<String>
    ): List<MissionGroup> {

        val alreadyAssigned =
            mutableSetOf<String>()

        return groups
            .map { group ->

                val validParticipantIds =
                    group
                        .participantIds
                        .filter { participantId ->

                            participantId in
                                    eligibleParticipantIds &&
                                    alreadyAssigned
                                        .add(
                                            participantId
                                        )
                        }

                val validSupportUserId =
                    group
                        .supportUserId
                        ?.takeIf {
                            it in
                                    validParticipantIds
                        }

                group.copy(
                    participantIds =
                        validParticipantIds,

                    supportUserId =
                        validSupportUserId
                )
            }
    }
}
