package br.com.ide.domain.model

import java.time.LocalDateTime

data class MissionEncounter(
    val id: String = "",
    val missionId: String,
    val registeredByUserId: String,
    val groupId: String? = null,
    val groupColorHex: String? = null,
    val personName: String? = null,
    val ageGroup: EncounterAgeGroup? = null,
    val phoneDigits: String? = null,
    val address: String? = null,
    val isAtPersonHome: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val performedActivities: List<MissionActivityType> = emptyList(),
    val bibleStudyStatus: BibleStudyStatus = BibleStudyStatus.NOT_OFFERED,
    val materials: List<MissionEncounterMaterial> = emptyList(),
    val surveyAnswers: List<MissionEncounterSurveyAnswer> = emptyList(),
    val acceptedFollowUp: Boolean = false,
    val notes: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)
