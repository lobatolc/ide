package br.com.ide.presentation.feature.createmission.participants

import br.com.ide.R
import br.com.ide.domain.model.Church
import br.com.ide.domain.model.District
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.usecase.GetChurchByIdUseCase
import br.com.ide.domain.usecase.GetChurchesByDistrictUseCase
import br.com.ide.domain.usecase.GetChurchesUseCase
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetDistrictsUseCase
import javax.inject.Inject

data class ParticipantsStepLoadResult(
    val creatorRole: UserRole,
    val creatorDistrictId: String? = null,
    val creatorChurchId: String? = null,
    val creatorChurchName: String = "",
    val districts: List<District> = emptyList(),
    val churches: List<Church> = emptyList(),
    val selectedDistrictIds: Set<String> = emptySet(),
    val selectedChurchIds: Set<String> = emptySet()
)

sealed interface ParticipantsStepLoadOutcome {

    data class Success(
        val result: ParticipantsStepLoadResult
    ) : ParticipantsStepLoadOutcome

    data class Error(
        val messageRes: Int
    ) : ParticipantsStepLoadOutcome
}

class ParticipantsStepLoader @Inject constructor(
    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,
    private val getDistrictsUseCase:
    GetDistrictsUseCase,
    private val getChurchesUseCase:
    GetChurchesUseCase,
    private val getChurchesByDistrictUseCase:
    GetChurchesByDistrictUseCase,
    private val getChurchByIdUseCase:
    GetChurchByIdUseCase
) {

    suspend fun load():
            ParticipantsStepLoadOutcome {

        val creator =
            getCurrentUserProfileUseCase()
                .getOrElse {

                    return ParticipantsStepLoadOutcome.Error(
                        R.string
                            .create_mission_participants_loading_error
                    )
                }

        return when (creator.role) {

            UserRole.LEADER -> {

                val churchId =
                    creator.churchId

                val districtId =
                    creator.districtId

                if (
                    churchId.isNullOrBlank() ||
                    districtId.isNullOrBlank()
                ) {
                    return ParticipantsStepLoadOutcome.Error(
                        R.string
                            .create_mission_creator_assignment_error
                    )
                }

                val church =
                    getChurchByIdUseCase(
                        churchId
                    )
                        .getOrElse {

                            return ParticipantsStepLoadOutcome.Error(
                                R.string
                                    .create_mission_participants_loading_error
                            )
                        }

                ParticipantsStepLoadOutcome.Success(
                    ParticipantsStepLoadResult(
                        creatorRole =
                            creator.role,
                        creatorDistrictId =
                            districtId,
                        creatorChurchId =
                            churchId,
                        creatorChurchName =
                            church.name,
                        districts =
                            emptyList(),
                        churches =
                            listOf(church),
                        selectedDistrictIds =
                            setOf(
                                districtId
                            ),
                        selectedChurchIds =
                            setOf(
                                churchId
                            )
                    )
                )
            }

            UserRole.PASTOR -> {

                val districtId =
                    creator.districtId

                if (
                    districtId.isNullOrBlank()
                ) {
                    return ParticipantsStepLoadOutcome.Error(
                        R.string
                            .create_mission_creator_assignment_error
                    )
                }

                val churches =
                    getChurchesByDistrictUseCase(
                        districtId
                    )
                        .getOrElse {

                            return ParticipantsStepLoadOutcome.Error(
                                R.string
                                    .create_mission_participants_loading_error
                            )
                        }

                ParticipantsStepLoadOutcome.Success(
                    ParticipantsStepLoadResult(
                        creatorRole =
                            creator.role,
                        creatorDistrictId =
                            districtId,
                        creatorChurchId =
                            null,
                        creatorChurchName =
                            "",
                        districts =
                            emptyList(),
                        churches =
                            churches,
                        selectedDistrictIds =
                            setOf(
                                districtId
                            ),
                        selectedChurchIds =
                            emptySet()
                    )
                )
            }

            UserRole.ADMIN -> {

                val districts =
                    getDistrictsUseCase()
                        .getOrElse {

                            return ParticipantsStepLoadOutcome.Error(
                                R.string
                                    .create_mission_participants_loading_error
                            )
                        }

                val churches =
                    getChurchesUseCase()
                        .getOrElse {

                            return ParticipantsStepLoadOutcome.Error(
                                R.string
                                    .create_mission_participants_loading_error
                            )
                        }

                ParticipantsStepLoadOutcome.Success(
                    ParticipantsStepLoadResult(
                        creatorRole =
                            creator.role,
                        creatorDistrictId =
                            null,
                        creatorChurchId =
                            null,
                        creatorChurchName =
                            "",
                        districts =
                            districts,
                        churches =
                            churches,
                        selectedDistrictIds =
                            emptySet(),
                        selectedChurchIds =
                            emptySet()
                    )
                )
            }

            UserRole.MISSIONARY -> {

                ParticipantsStepLoadOutcome.Error(
                    R.string
                        .create_mission_permission_error
                )
            }
        }
    }
}