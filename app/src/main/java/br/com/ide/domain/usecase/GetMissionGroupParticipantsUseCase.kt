package br.com.ide.domain.usecase

import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.repository.ChurchRepository
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class GetMissionGroupParticipantsUseCase @Inject constructor(
    private val churchRepository:
    ChurchRepository,

    private val userRepository:
    UserRepository
) {

    suspend operator fun invoke(
        participatingChurchIds: List<String>
    ): Result<List<UserProfile>> {

        return try {

            if (
                participatingChurchIds.isEmpty()
            ) {
                return Result.success(
                    emptyList()
                )
            }

            val usersById =
                linkedMapOf<String, UserProfile>()

            // =====================================================
            // Missionários e líderes das igrejas participantes
            // =====================================================

            participatingChurchIds
                .distinct()
                .forEach { churchId ->

                    val churchUsers =
                        userRepository
                            .getUsersByChurch(
                                churchId
                            )
                            .getOrThrow()

                    churchUsers
                        .filter { user ->

                            user.role ==
                                    UserRole.MISSIONARY ||
                                    user.role ==
                                    UserRole.LEADER
                        }
                        .forEach { user ->

                            usersById[
                                user.id
                            ] =
                                user
                        }
                }

            // =====================================================
            // Descobrir distritos das igrejas participantes
            // =====================================================

            val districtIds =
                participatingChurchIds
                    .distinct()
                    .mapNotNull { churchId ->

                        churchRepository
                            .getChurchById(
                                churchId
                            )
                            .getOrNull()
                            ?.districtId
                    }
                    .distinct()

            // =====================================================
            // Pastores dos distritos participantes
            // =====================================================

            districtIds
                .forEach { districtId ->

                    val districtUsers =
                        userRepository
                            .getUsersByDistrict(
                                districtId
                            )
                            .getOrThrow()

                    districtUsers
                        .filter { user ->

                            user.role ==
                                    UserRole.PASTOR
                        }
                        .forEach { user ->

                            usersById[
                                user.id
                            ] =
                                user
                        }
                }

            val users =
                usersById
                    .values
                    .sortedWith(
                        compareBy(
                            UserProfile::firstName,
                            UserProfile::lastName
                        )
                    )

            Result.success(
                users
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }
}