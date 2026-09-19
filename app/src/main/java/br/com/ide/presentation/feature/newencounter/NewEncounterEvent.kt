package br.com.ide.presentation.feature.newencounter

import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.EncounterAgeGroup
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType

sealed interface NewEncounterEvent {

    // -------------------------------------------------
    // Pessoa encontrada
    // -------------------------------------------------

    data class PersonNameChanged(
        val value: String
    ) : NewEncounterEvent

    data class AgeGroupChanged(
        val value: EncounterAgeGroup?
    ) : NewEncounterEvent

    data class PhoneChanged(
        val value: String
    ) : NewEncounterEvent

    data class AddressChanged(
        val value: String
    ) : NewEncounterEvent

    // -------------------------------------------------
    // Localização
    // -------------------------------------------------

    data class LocationPermissionChanged(
        val granted: Boolean
    ) : NewEncounterEvent

    data object RefreshLocation :
        NewEncounterEvent

    data class PersonHomeChanged(
        val value: Boolean
    ) : NewEncounterEvent

    // -------------------------------------------------
    // Ações
    // -------------------------------------------------

    data class ActivityToggled(
        val activity: MissionActivityType
    ) : NewEncounterEvent

    data class BibleStudyStatusChanged(
        val status: BibleStudyStatus
    ) : NewEncounterEvent

    data class MaterialQuantityChanged(
        val material: MissionMaterialType,
        val quantity: Int
    ) : NewEncounterEvent

    data class SurveyAnswerChanged(
        val questionId: String,
        val answer: String
    ) : NewEncounterEvent

    // -------------------------------------------------
    // Acompanhamento
    // -------------------------------------------------

    data class AcceptedFollowUpChanged(
        val value: Boolean
    ) : NewEncounterEvent

    data class NotesChanged(
        val value: String
    ) : NewEncounterEvent

    data class ContactNowChanged(
        val value: Boolean
    ) : NewEncounterEvent

    // -------------------------------------------------
    // Controle
    // -------------------------------------------------

    data object SaveClicked :
        NewEncounterEvent
}
