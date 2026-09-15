package br.com.ide.presentation.feature.missiongroups

sealed interface MissionGroupsEvent {

    data class CreateGroup(
        val name: String
    ) : MissionGroupsEvent

    data class RenameGroup(
        val groupId: String,
        val name: String
    ) : MissionGroupsEvent

    data class DeleteGroup(
        val groupId: String
    ) : MissionGroupsEvent

    data class AddParticipant(
        val groupId: String,
        val participantId: String
    ) : MissionGroupsEvent

    data class RemoveParticipant(
        val groupId: String,
        val participantId: String
    ) : MissionGroupsEvent

    data class SetSupport(
        val groupId: String,
        val participantId: String?
    ) : MissionGroupsEvent

    data object Save :
        MissionGroupsEvent
}