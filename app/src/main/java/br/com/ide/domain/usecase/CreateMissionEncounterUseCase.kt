package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.repository.MissionEncounterRepository
import javax.inject.Inject

class CreateMissionEncounterUseCase @Inject constructor(
    private val repository: MissionEncounterRepository
) {

    suspend operator fun invoke(
        encounter: MissionEncounter
    ): Result<String> {

        val normalized =
            encounter.copy(
                personName = encounter.personName
                    ?.trim()
                    ?.takeIf(String::isNotBlank),
                phoneDigits = encounter.phoneDigits
                    ?.filter(Char::isDigit)
                    ?.takeIf(String::isNotBlank),
                address = encounter.address
                    ?.trim()
                    ?.takeIf(String::isNotBlank),
                notes = encounter.notes
                    ?.trim()
                    ?.takeIf(String::isNotBlank),
                performedActivities = encounter.performedActivities
                    .distinct(),
                materials = encounter.materials
                    .filter { it.quantity > 0 },
                surveyAnswers = encounter.surveyAnswers
                    .map { answer ->
                        answer.copy(
                            answer = answer.answer.trim()
                        )
                    }
                    .filter { it.answer.isNotBlank() }
            )

        return repository.createEncounter(
            normalized
        )
    }
}
