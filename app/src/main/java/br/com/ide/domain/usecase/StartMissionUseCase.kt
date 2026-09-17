package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.repository.MissionParticipantRepository
import br.com.ide.domain.repository.MissionRepository
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class StartMissionUseCase @Inject constructor(
    private val missionRepository:
    MissionRepository,
    private val missionParticipantRepository:
    MissionParticipantRepository,
    private val userRepository:
    UserRepository
) {

    suspend operator fun invoke(
        missionId: String
    ): Result<Unit> {

        val mission =
            try {
                missionRepository
                    .getMissionById(
                        missionId =
                            missionId
                    )
            } catch (
                exception: Exception
            ) {
                return Result.failure(
                    exception
                )
            }
                ?: return Result.failure(
                    IllegalStateException(
                        "Mission not found."
                    )
                )

        /*
         * Primeiro carregamos todos os usuários das igrejas
         * participantes da missão.
         *
         * Isso é importante porque um missionário pode
         * participar da missão sem ainda estar atribuído
         * a um grupo.
         */
        val eligibleUsersById =
            linkedMapOf<
                    String,
                    br.com.ide.domain.model.UserProfile
                    >()

        mission
            .participatingChurchIds
            .filter {
                it.isNotBlank()
            }
            .distinct()
            .forEach { churchId ->

                val users =
                    userRepository
                        .getUsersByChurch(
                            churchId
                        )
                        .getOrElse {
                            return Result.failure(
                                it
                            )
                        }

                users
                    .filter {
                        it.id.isNotBlank()
                    }
                    .forEach { user ->

                        eligibleUsersById[
                            user.id
                        ] =
                            user
                    }
            }

        val participantsByUserId =
            linkedMapOf<
                    String,
                    MissionParticipantState
                    >()

        /*
         * Todos os usuários elegíveis entram inicialmente
         * sem grupo.
         */
        eligibleUsersById
            .keys
            .forEach { userId ->

                participantsByUserId[
                    userId
                ] =
                    MissionParticipantState(
                        userId =
                            userId,
                        missionId =
                            missionId,
                        groupId =
                            null,
                        isSupport =
                            false,
                        status =
                            MissionParticipantStatus.ACTIVE
                    )
            }

        /*
         * Em seguida aplicamos as atribuições dos grupos.
         *
         * Se o usuário estiver em um grupo, groupId passa a
         * apontar para ele. Se for apoio, isSupport = true.
         */
        mission.groups
            .forEach { group ->

                group.participantIds
                    .filter {
                        it.isNotBlank()
                    }
                    .forEach { userId ->

                        val current =
                            participantsByUserId[
                                userId
                            ]

                        val isSupport =
                            group.supportUserId ==
                                    userId

                        participantsByUserId[
                            userId
                        ] =
                            if (
                                current != null
                            ) {
                                current.copy(
                                    groupId =
                                        group.id,
                                    isSupport =
                                        current.isSupport ||
                                                isSupport
                                )
                            } else {
                                /*
                                 * Compatibilidade:
                                 * se um usuário estiver salvo em
                                 * um grupo antigo, mesmo que não
                                 * apareça mais na consulta da igreja,
                                 * ainda preservamos sua participação.
                                 */
                                MissionParticipantState(
                                    userId =
                                        userId,
                                    missionId =
                                        missionId,
                                    groupId =
                                        group.id,
                                    isSupport =
                                        isSupport,
                                    status =
                                        MissionParticipantStatus.ACTIVE
                                )
                            }
                    }

                group.supportUserId
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { supportUserId ->

                        val current =
                            participantsByUserId[
                                supportUserId
                            ]

                        participantsByUserId[
                            supportUserId
                        ] =
                            if (
                                current != null
                            ) {
                                current.copy(
                                    groupId =
                                        group.id,
                                    isSupport =
                                        true
                                )
                            } else {
                                MissionParticipantState(
                                    userId =
                                        supportUserId,
                                    missionId =
                                        missionId,
                                    groupId =
                                        group.id,
                                    isSupport =
                                        true,
                                    status =
                                        MissionParticipantStatus.ACTIVE
                                )
                            }
                    }
            }

        missionParticipantRepository
            .initializeParticipants(
                participants =
                    participantsByUserId
                        .values
                        .toList()
            )
            .onFailure {
                return Result.failure(
                    it
                )
            }

        return missionRepository
            .startMission(
                missionId =
                    missionId
            )
    }
}
