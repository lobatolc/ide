package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class GetVisibleMissionTrackUserIdsUseCase @Inject constructor(
    private val userRepository:
    UserRepository
) {

    suspend operator fun invoke(
        currentUser: UserProfile,
        participants: List<MissionParticipantState>
    ): Set<String> {

        val participantIds =
            participants
                .map {
                    it.userId
                }
                .toSet()

        if (
            participantIds.isEmpty()
        ) {
            return emptySet()
        }

        return when (
            currentUser.role
        ) {

            UserRole.MISSIONARY -> {

                val currentParticipant =
                    participants
                        .firstOrNull {
                            it.userId ==
                                    currentUser.id
                        }
                        ?: return emptySet()

                val groupId =
                    currentParticipant
                        .groupId

                /*
                 * groupId == null significa Grupo Geral. Isso vale mesmo
                 * quando a missão também possui grupos configurados.
                 */
                if (
                    groupId == null
                ) {
                    return participants
                        .asSequence()
                        .filter {
                            it.groupId == null
                        }
                        .map {
                            it.userId
                        }
                        .toSet()
                }

                participants
                    .asSequence()
                    .filter {
                        it.groupId ==
                                groupId
                    }
                    .map {
                        it.userId
                    }
                    .toSet()
            }

            UserRole.LEADER -> {

                val churchId =
                    currentUser
                        .churchId
                        ?: return emptySet()

                userRepository
                    .getUsersByChurch(
                        churchId
                    )
                    .getOrElse {
                        emptyList()
                    }
                    .asSequence()
                    .map {
                        it.id
                    }
                    .filter {
                        it in
                                participantIds
                    }
                    .toSet()
            }

            UserRole.PASTOR -> {

                val districtId =
                    currentUser
                        .districtId
                        ?: return emptySet()

                userRepository
                    .getUsersByDistrict(
                        districtId
                    )
                    .getOrElse {
                        emptyList()
                    }
                    .asSequence()
                    .map {
                        it.id
                    }
                    .filter {
                        it in
                                participantIds
                    }
                    .toSet()
            }

            UserRole.ADMIN -> {
                participantIds
            }
        }
    }
}
