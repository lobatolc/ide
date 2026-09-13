package br.com.ide.data.repository

import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionLocation
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionMovement
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.model.MissionSurveyQuestion
import br.com.ide.domain.model.SurveyQuestionType
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.repository.MissionRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class FirestoreMissionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MissionRepository {

    override suspend fun getMissions(): List<Mission> {

        return firestore
            .collection("missions")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toMission()
            }
    }

    override suspend fun getMissionById(
        missionId: String
    ): Mission? {

        val document =
            firestore
                .collection("missions")
                .document(missionId)
                .get()
                .await()

        if (
            !document.exists()
        ) {
            return null
        }

        return document.toMission()
    }

    override suspend fun createMission(
        mission: Mission
    ): Result<String> {

        return try {

            val document =
                if (
                    mission.id.isBlank()
                ) {
                    firestore
                        .collection("missions")
                        .document()
                } else {
                    firestore
                        .collection("missions")
                        .document(
                            mission.id
                        )
                }

            document
                .set(
                    mission.toFirestoreMap()
                )
                .await()

            Result.success(
                document.id
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    override suspend fun updateMission(
        mission: Mission
    ): Result<Unit> {

        return try {

            firestore
                .collection("missions")
                .document(
                    mission.id
                )
                .set(
                    mission.toFirestoreMap()
                )
                .await()

            Result.success(
                Unit
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    override suspend fun updateMissionLocations(
        missionId: String,
        departureLocation: MissionLocation,
        returnLocation: MissionLocation?
    ): Result<Unit> {

        return try {

            firestore
                .collection("missions")
                .document(missionId)
                .update(
                    mapOf(
                        "departureLocation" to
                                departureLocation
                                    .toFirestoreMap(),

                        "returnLocation" to
                                returnLocation
                                    ?.toFirestoreMap()
                    )
                )
                .await()

            Result.success(
                Unit
            )

        } catch (
            exception: Exception
        ) {

            Result.failure(
                exception
            )
        }
    }

    // =========================================================
    // Firestore -> Mission
    // =========================================================

    private fun DocumentSnapshot.toMission():
            Mission? {

        val scheduledTimestamp =
            getTimestamp(
                "scheduledAt"
            )
                ?: return null

        val status =
            getString(
                "status"
            )
                ?.let {
                    runCatching {
                        MissionStatus.valueOf(
                            it
                        )
                    }.getOrNull()
                }
                ?: return null

        val movement =
            getString(
                "movement"
            )
                ?.let {
                    runCatching {
                        MissionMovement.valueOf(
                            it
                        )
                    }.getOrNull()
                }
                ?: return null

        val creatorRole =
            getString(
                "creatorRole"
            )
                ?.let {
                    runCatching {
                        UserRole.valueOf(
                            it
                        )
                    }.getOrNull()
                }
                ?: return null

        val activities =
            getStringList(
                "activities"
            )
                .mapNotNull { value ->

                    runCatching {
                        MissionActivityType.valueOf(
                            value
                        )
                    }.getOrNull()
                }

        val materials =
            getStringList(
                "materials"
            )
                .mapNotNull { value ->

                    runCatching {
                        MissionMaterialType.valueOf(
                            value
                        )
                    }.getOrNull()
                }

        val surveyQuestions =
            readSurveyQuestions()

        return Mission(
            id =
                id,

            // -------------------------------------------------
            // Geral
            // -------------------------------------------------

            name =
                getString(
                    "name"
                ).orEmpty(),

            scheduledAt =
                scheduledTimestamp
                    .toLocalDateTime(),

            description =
                getString(
                    "description"
                ).orEmpty(),

            movement =
                movement,

            customMovementName =
                getString(
                    "customMovementName"
                ),

            // -------------------------------------------------
            // Participantes
            // -------------------------------------------------

            participatingChurchIds =
                getStringList(
                    "participatingChurchIds"
                ),

            // -------------------------------------------------
            // Ações
            // -------------------------------------------------

            activities =
                activities,

            customActivityName =
                getString(
                    "customActivityName"
                ),

            // -------------------------------------------------
            // Materiais
            // -------------------------------------------------

            materials =
                materials,

            customMaterialName =
                getString(
                    "customMaterialName"
                ),

            // -------------------------------------------------
            // Pesquisa
            // -------------------------------------------------

            surveyQuestions =
                surveyQuestions,

            // -------------------------------------------------
            // Planejamento
            // -------------------------------------------------

            departureLocation =
                readLocation(
                    "departureLocation"
                ),

            returnLocation =
                readLocation(
                    "returnLocation"
                ),

            // -------------------------------------------------
            // Controle
            // -------------------------------------------------

            status =
                status,

            createdBy =
                getString(
                    "createdBy"
                ).orEmpty(),

            creatorRole =
                creatorRole,

            creatorChurchId =
                getString(
                    "creatorChurchId"
                ),

            creatorDistrictId =
                getString(
                    "creatorDistrictId"
                ),

            // -------------------------------------------------
            // Dados posteriores
            // -------------------------------------------------

            photoUrl =
                getString(
                    "photoUrl"
                ),

            totalDurationMinutes =
                getLong(
                    "totalDurationMinutes"
                )
        )
    }

    // =========================================================
    // Listas simples
    // =========================================================

    private fun DocumentSnapshot
            .getStringList(
        field: String
    ): List<String> {

        return (
                get(
                    field
                ) as? List<*>
                )
            ?.mapNotNull {
                it as? String
            }
            ?: emptyList()
    }

    // =========================================================
    // Pesquisa
    // =========================================================

    private fun DocumentSnapshot
            .readSurveyQuestions():
            List<MissionSurveyQuestion> {

        return (
                get(
                    "surveyQuestions"
                ) as? List<*>
                )
            ?.mapNotNull { item ->

                val map =
                    item as? Map<*, *>
                        ?: return@mapNotNull null

                val id =
                    map["id"]
                            as? String
                        ?: return@mapNotNull null

                val question =
                    map["question"]
                            as? String
                        ?: return@mapNotNull null

                val type =
                    (
                            map["type"]
                                    as? String
                            )
                        ?.let { value ->

                            runCatching {

                                SurveyQuestionType
                                    .valueOf(
                                        value
                                    )

                            }.getOrNull()
                        }
                        ?: return@mapNotNull null

                val options =
                    (
                            map["options"]
                                    as? List<*>
                            )
                        ?.mapNotNull {
                            it as? String
                        }
                        ?: emptyList()

                MissionSurveyQuestion(
                    id =
                        id,

                    question =
                        question,

                    type =
                        type,

                    options =
                        options
                )
            }
            ?: emptyList()
    }

    // =========================================================
    // Localização
    // =========================================================

    private fun DocumentSnapshot.readLocation(
        field: String
    ): MissionLocation? {

        val map =
            get(
                field
            ) as? Map<*, *>
                ?: return null

        val name =
            map["name"]
                    as? String
                ?: return null

        val address =
            map["address"]
                    as? String
                ?: ""

        val latitude =
            map["latitude"]
                    as? Number
                ?: return null

        val longitude =
            map["longitude"]
                    as? Number
                ?: return null

        return MissionLocation(
            name =
                name,

            address =
                address,

            latitude =
                latitude
                    .toDouble(),

            longitude =
                longitude
                    .toDouble()
        )
    }

    // =========================================================
    // Mission -> Firestore
    // =========================================================

    private fun Mission.toFirestoreMap():
            Map<String, Any?> {

        return mapOf(

            // -------------------------------------------------
            // Geral
            // -------------------------------------------------

            "name" to
                    name,

            "scheduledAt" to
                    scheduledAt
                        .toTimestamp(),

            "description" to
                    description,

            "movement" to
                    movement.name,

            "customMovementName" to
                    customMovementName,

            // -------------------------------------------------
            // Participantes
            // -------------------------------------------------

            "participatingChurchIds" to
                    participatingChurchIds,

            // -------------------------------------------------
            // Ações
            // -------------------------------------------------

            "activities" to
                    activities
                        .map {
                            it.name
                        },

            "customActivityName" to
                    customActivityName,

            // -------------------------------------------------
            // Materiais
            // -------------------------------------------------

            "materials" to
                    materials
                        .map {
                            it.name
                        },

            "customMaterialName" to
                    customMaterialName,

            // -------------------------------------------------
            // Pesquisa
            // -------------------------------------------------

            "surveyQuestions" to
                    surveyQuestions
                        .map { question ->

                            mapOf(
                                "id" to
                                        question.id,

                                "question" to
                                        question.question,

                                "type" to
                                        question.type.name,

                                "options" to
                                        question.options
                            )
                        },

            // -------------------------------------------------
            // Planejamento
            // -------------------------------------------------

            "departureLocation" to
                    departureLocation
                        ?.toFirestoreMap(),

            "returnLocation" to
                    returnLocation
                        ?.toFirestoreMap(),

            // -------------------------------------------------
            // Controle
            // -------------------------------------------------

            "status" to
                    status.name,

            "createdBy" to
                    createdBy,

            "creatorRole" to
                    creatorRole.name,

            "creatorChurchId" to
                    creatorChurchId,

            "creatorDistrictId" to
                    creatorDistrictId,

            // -------------------------------------------------
            // Dados posteriores
            // -------------------------------------------------

            "photoUrl" to
                    photoUrl,

            "totalDurationMinutes" to
                    totalDurationMinutes
        )
    }

    // =========================================================
    // MissionLocation -> Firestore
    // =========================================================

    private fun MissionLocation.toFirestoreMap():
            Map<String, Any?> {

        return mapOf(
            "name" to
                    name,

            "address" to
                    address,

            "latitude" to
                    latitude,

            "longitude" to
                    longitude
        )
    }

    // =========================================================
    // Datas
    // =========================================================

    private fun LocalDateTime.toTimestamp():
            Timestamp {

        val instant =
            atZone(
                ZoneId.systemDefault()
            )
                .toInstant()

        return Timestamp(
            instant.epochSecond,
            instant.nano
        )
    }

    private fun Timestamp.toLocalDateTime():
            LocalDateTime {

        return Instant
            .ofEpochSecond(
                seconds,
                nanoseconds.toLong()
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .toLocalDateTime()
    }
}