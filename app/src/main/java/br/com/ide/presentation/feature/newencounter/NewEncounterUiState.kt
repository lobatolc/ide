package br.com.ide.presentation.feature.newencounter

import androidx.annotation.StringRes
import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.EncounterAgeGroup
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionSurveyQuestion

data class NewEncounterUiState(

    // -------------------------------------------------
    // Missão
    // -------------------------------------------------

    val missionId: String = "",
    val missionName: String = "",

    val availableActivities: List<MissionActivityType> =
        emptyList(),

    val customActivityName: String? = null,

    val availableMaterials: List<MissionMaterialType> =
        emptyList(),

    val customMaterialName: String? = null,

    val surveyQuestions: List<MissionSurveyQuestion> =
        emptyList(),

    // -------------------------------------------------
    // Pessoa encontrada
    // -------------------------------------------------

    val personName: String = "",

    val ageGroup: EncounterAgeGroup? = null,

    /*
     * Mantemos somente os dígitos no estado.
     *
     * Exemplo:
     * 91987654321
     *
     * A tela será responsável por exibir:
     * (91) 9 8765-4321
     */
    val phoneDigits: String = "",

    @StringRes
    val phoneErrorMessageRes: Int? = null,

    val address: String = "",

    // -------------------------------------------------
    // Localização do encontro
    // -------------------------------------------------

    val hasLocationPermission: Boolean = false,

    val isLoadingLocation: Boolean = false,

    val latitude: Double? = null,

    val longitude: Double? = null,

    /*
     * Marcado somente quando o usuário confirmar que
     * o encontro aconteceu na casa da pessoa.
     *
     * Nesse caso, a localização atual poderá ser usada
     * para preencher o endereço por geocodificação reversa.
     */
    val isAtPersonHome: Boolean = false,

    val isResolvingAddress: Boolean = false,

    // -------------------------------------------------
    // Ações realizadas
    // -------------------------------------------------

    val performedActivities: Set<MissionActivityType> =
        emptySet(),

    val bibleStudyStatus: BibleStudyStatus =
        BibleStudyStatus.NOT_OFFERED,

    /*
     * Quantidade entregue por tipo de material.
     *
     * Somente materiais configurados na missão serão
     * exibidos pela tela.
     */
    val materialQuantities:
    Map<MissionMaterialType, Int> =
        emptyMap(),

    /*
     * Chave: id da pergunta da missão.
     * Valor: resposta informada pelo usuário.
     */
    val surveyAnswers:
    Map<String, String> =
        emptyMap(),

    // -------------------------------------------------
    // Acompanhamento
    // -------------------------------------------------

    val acceptedFollowUp: Boolean = false,

    val notes: String = "",

    /*
     * Estado exclusivo da tela.
     *
     * Não será persistido no Firestore.
     * Após salvar, se estiver true e houver telefone
     * válido, o app abrirá o WhatsApp.
     */
    val contactNow: Boolean = false,

    // -------------------------------------------------
    // Controle da tela
    // -------------------------------------------------

    val isLoading: Boolean = true,

    val isSaving: Boolean = false,

    @StringRes
    val errorMessageRes: Int? = null
) {

    val hasValidPhone: Boolean
        get() =
            phoneDigits.length == 11

    val canContactNow: Boolean
        get() =
            hasValidPhone

    val hasLocation: Boolean
        get() =
            latitude != null &&
                    longitude != null
}
