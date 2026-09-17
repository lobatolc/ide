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
                 * Missionário sem grupo continua vendo
                 * o próprio trajeto, mas não tratamos
                 * "sem grupo" como um grupo coletivo.
                 *
                 * Assim evitamos que todos os não atribuídos
                 * enxerguem os trajetos uns dos outros.
                 */
                if (
                    groupId == null
                ) {
                    return setOf(
                        currentUser.id
                    )
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

                /*
                 * Administrador possui visão operacional
                 * completa da missão.
                 *
                 * A redução visual ficará a cargo do filtro
                 * disponível no menu da execução.
                 */
                participantIds
            }
        }
    }
}
