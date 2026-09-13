package br.com.ide.domain.model

import java.time.LocalDateTime

data class Mission(
    val id: String,

    // Geral
    val name: String,
    val scheduledAt: LocalDateTime,
    val description: String,

    val movement: MissionMovement,
    val customMovementName: String? = null,

    // Participantes
    val participatingChurchIds: List<String>,

    // Ações
    val activities: List<MissionActivityType>,
    val customActivityName: String? = null,

    // Materiais
    val materials: List<MissionMaterialType> = emptyList(),
    val customMaterialName: String? = null,

    // Pesquisa
    val surveyQuestions: List<MissionSurveyQuestion> = emptyList(),

    // Controle
    val status: MissionStatus = MissionStatus.PLANNING,

    val createdBy: String,
    val creatorRole: UserRole,
    val creatorChurchId: String? = null,
    val creatorDistrictId: String? = null,

    // Dados preenchidos posteriormente
    val photoUrl: String? = null,
    val totalDurationMinutes: Long? = null
)