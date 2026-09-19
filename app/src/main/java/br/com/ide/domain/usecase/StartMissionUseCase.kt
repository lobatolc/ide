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

        val eligibleUsersById =
            linkedMapOf<
                    String,
                    br.com.ide.domain.model.UserProfile
                    >()

        /*
         * Igrejas efetivamente participantes.
         *
         * Em missões locais antigas/sem grupos, participatingChurchIds
         * pode estar vazio. Nesse caso usamos a igreja do criador para
         * que a missão não seja iniciada sem nenhum participante.
         */
        val configuredChurchIds =
            mission
                .participatingChurchIds
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        val effectiveChurchIds =
            if (
                configuredChurchIds.isNotEmpty()
            ) {
                configuredChurchIds
            } else {
                listOfNotNull(
                    mission.creatorChurchId
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .distinct()
            }

        effectiveChurchIds
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
         * Todos os usuários elegíveis entram inicialmente sem groupId.
         *
         * groupId = null representa o Grupo Geral na camada de execução,
         * mesmo quando a missão também possui grupos configurados. Assim,
         * toda pessoa não atribuída fica reunida no mesmo grupo lógico.
         * Não persistimos um grupo artificial no documento da missão.
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
         * Se existem grupos explícitos, aplicamos suas atribuições.
         * Participantes salvos em grupos continuam sendo preservados mesmo
         * que não apareçam mais na consulta da igreja.
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

        /*
         * Nunca colocamos a missão em andamento sem participantes.
         * Isso evita gerar uma missão válida cujo usuário atual não possua
         * missions/{missionId}/participants/{userId}.
         */
        if (
            participantsByUserId.isEmpty()
        ) {
            return Result.failure(
                IllegalStateException(
                    "Mission has no participants to initialize."
                )
            )
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
