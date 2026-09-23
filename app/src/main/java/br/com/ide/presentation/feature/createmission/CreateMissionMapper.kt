package br.com.ide.presentation.feature.createmission

import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionMovement
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.model.MissionSurveyQuestion
import java.time.LocalDateTime
import javax.inject.Inject

class CreateMissionMapper @Inject constructor() {

    fun map(
        state: CreateMissionUiState,
        createdBy: String
    ): Mission {

        val date =
            requireNotNull(
                state.date
            ) {
                "Mission date must not be null."
            }

        val time =
            requireNotNull(
                state.time
            ) {
                "Mission time must not be null."
            }

        val movement =
            requireNotNull(
                state.movement
            ) {
                "Mission movement must not be null."
            }

        val creatorRole =
            requireNotNull(
                state.creatorRole
            ) {
                "Creator role must not be null."
            }

        return Mission(
            id = "",

            name =
                state.name.trim(),

            scheduledAt =
                LocalDateTime.of(
                    date,
                    time
                ),

            description =
                state.description.trim(),

            movement =
                movement,

            customMovementName =
                state.customMovementName
                    .trim()
                    .takeIf {
                        movement ==
                                MissionMovement.OTHER &&
                                it.isNotBlank()
                    },

            participatingChurchIds =
                state.selectedChurchIds
                    .toList(),

            activities =
                state.selectedActivities
                    .toList(),

            customActivityName =
                state.customActivityName
                    .trim()
                    .takeIf {
                        MissionActivityType.OTHER in
                                state.selectedActivities &&
                                it.isNotBlank()
                    },

            materials =
                state.selectedMaterials
                    .toList(),

            customMaterialName =
                state.customMaterialName
                    .trim()
                    .takeIf {
                        MissionMaterialType.OTHER in
                                state.selectedMaterials &&
                                it.isNotBlank()
                    },

            surveyQuestions =
                state.surveyQuestions
                    .map { question ->

                        MissionSurveyQuestion(
                            id =
                                question.id,

                            question =
                                question.question
                                    .trim(),

                            type =
                                question.type,

                            options =
                                question.options
                                    .map {
                                        it.trim()
                                    }
                                    .filter {
                                        it.isNotBlank()
                                    }
                        )
                    },

            status =
                MissionStatus.PLANNING,

            createdBy =
                createdBy,

            creatorRole =
                creatorRole,

            creatorChurchId =
                state.creatorChurchId,

            creatorDistrictId =
                state.creatorDistrictId,

            photoUrl =
                null,

            totalDurationMinutes =
                null
        )
    }
}