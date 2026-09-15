package br.com.ide.presentation.feature.missiongroups

import androidx.annotation.StringRes
import br.com.ide.domain.model.MissionGroup
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.model.UserProfile

data class MissionGroupsUiState(

    // Missão
    val missionId: String = "",
    val missionName: String = "",
    val missionStatus: MissionStatus? = null,

    // Participantes disponíveis dentro do escopo da missão
    val eligibleParticipants: List<UserProfile> =
        emptyList(),

    // Grupos configurados
    val groups: List<MissionGroup> =
        emptyList(),

    // Controle
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,

    @StringRes
    val errorMessage: Int? = null
) {

    val canEdit: Boolean
        get() =
            missionStatus ==
                    MissionStatus.PLANNING ||
                    missionStatus ==
                    MissionStatus.SCHEDULED

    val assignedParticipantIds: Set<String>
        get() =
            groups
                .flatMap {
                    it.participantIds
                }
                .toSet()

    val unassignedParticipants: List<UserProfile>
        get() =
            eligibleParticipants
                .filterNot { participant ->
                    participant.id in
                            assignedParticipantIds
                }
}