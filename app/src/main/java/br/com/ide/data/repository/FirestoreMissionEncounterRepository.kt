package br.com.ide.data.repository

import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.EncounterAgeGroup
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionEncounterMaterial
import br.com.ide.domain.model.MissionEncounterSurveyAnswer
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.SurveyQuestionType
import br.com.ide.domain.repository.MissionEncounterRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreMissionEncounterRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MissionEncounterRepository {

    override suspend fun createEncounter(
        encounter: MissionEncounter
    ): Result<String> {
        return try {
            val document =
                if (encounter.id.isBlank()) {
                    encountersCollection(encounter.missionId).document()
                } else {
                    encountersCollection(encounter.missionId).document(encounter.id)
                }

            val data = encounter
                .toFirestoreMap()
                .toMutableMap()
                .apply {
                    this["id"] = document.id
                    this["createdAt"] = FieldValue.serverTimestamp()
                    this["updatedAt"] = null
                }

            document.set(data).await()
            Result.success(document.id)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun getMissionEncounters(
        missionId: String
    ): Result<List<MissionEncounter>> {
        return try {
            val encounters = encountersCollection(missionId)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toMissionEncounter(
                        fallbackMissionId = missionId
                    )
                }
                .sortedBy { it.createdAt }

            Result.success(encounters)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override fun observeMissionEncounters(
        missionId: String
    ): Flow<List<MissionEncounter>> =
        callbackFlow {
            val registration = encountersCollection(missionId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val encounters = snapshot
                        ?.documents
                        ?.mapNotNull { document ->
                            document.toMissionEncounter(
                                fallbackMissionId = missionId
                            )
                        }
                        ?.sortedBy { it.createdAt }
                        ?: emptyList()

                    trySend(encounters)
                }

            awaitClose {
                registration.remove()
            }
        }

    private fun encountersCollection(
        missionId: String
    ) =
        firestore
            .collection("missions")
            .document(missionId)
            .collection("encounters")

    private fun MissionEncounter.toFirestoreMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "missionId" to missionId,
            "registeredByUserId" to registeredByUserId,
            "groupId" to groupId,
            "groupColorHex" to groupColorHex,
            "personName" to personName,
            "ageGroup" to ageGroup?.name,
            "phoneDigits" to phoneDigits,
            "address" to address,
            "isAtPersonHome" to isAtPersonHome,
            "latitude" to latitude,
            "longitude" to longitude,
            "performedActivities" to performedActivities.map(MissionActivityType::name),
            "bibleStudyStatus" to bibleStudyStatus.name,
            "materials" to materials.map { material ->
                mapOf(
                    "type" to material.type.name,
                    "customName" to material.customName,
                    "quantity" to material.quantity
                )
            },
            "surveyAnswers" to surveyAnswers.map { answer ->
                mapOf(
                    "questionId" to answer.questionId,
                    "question" to answer.question,
                    "type" to answer.type.name,
                    "answer" to answer.answer
                )
            },
            "acceptedFollowUp" to acceptedFollowUp,
            "notes" to notes
        )

    private fun DocumentSnapshot.toMissionEncounter(
        fallbackMissionId: String
    ): MissionEncounter? {
        val missionId = getString("missionId")
            ?.takeIf { it.isNotBlank() }
            ?: fallbackMissionId

        val registeredByUserId = getString("registeredByUserId")
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val ageGroup = getString("ageGroup")
            ?.let { value ->
                runCatching {
                    EncounterAgeGroup.valueOf(value)
                }.getOrNull()
            }

        val bibleStudyStatus = getString("bibleStudyStatus")
            ?.let { value ->
                runCatching {
                    BibleStudyStatus.valueOf(value)
                }.getOrNull()
            }
            ?: BibleStudyStatus.NOT_OFFERED

        val performedActivities =
            (get("performedActivities") as? List<*>)
                ?.mapNotNull { value ->
                    val activityName = value as? String
                        ?: return@mapNotNull null

                    runCatching {
                        MissionActivityType.valueOf(activityName)
                    }.getOrNull()
                }
                ?: emptyList()

        val materials =
            (get("materials") as? List<*>)
                ?.mapNotNull { item ->
                    val map = item as? Map<*, *>
                        ?: return@mapNotNull null

                    val type = (map["type"] as? String)
                        ?.let { value ->
                            runCatching {
                                MissionMaterialType.valueOf(value)
                            }.getOrNull()
                        }
                        ?: return@mapNotNull null

                    val quantity = (map["quantity"] as? Number)
                        ?.toInt()
                        ?.coerceAtLeast(0)
                        ?: 0

                    if (quantity <= 0) {
                        return@mapNotNull null
                    }

                    MissionEncounterMaterial(
                        type = type,
                        customName = map["customName"] as? String,
                        quantity = quantity
                    )
                }
                ?: emptyList()

        val surveyAnswers =
            (get("surveyAnswers") as? List<*>)
                ?.mapNotNull { item ->
                    val map = item as? Map<*, *>
                        ?: return@mapNotNull null

                    val questionId = map["questionId"] as? String
                        ?: return@mapNotNull null
                    val question = map["question"] as? String
                        ?: return@mapNotNull null
                    val type = (map["type"] as? String)
                        ?.let { value ->
                            runCatching {
                                SurveyQuestionType.valueOf(value)
                            }.getOrNull()
                        }
                        ?: return@mapNotNull null
                    val answer = map["answer"] as? String
                        ?: return@mapNotNull null

                    MissionEncounterSurveyAnswer(
                        questionId = questionId,
                        question = question,
                        type = type,
                        answer = answer
                    )
                }
                ?: emptyList()

        return MissionEncounter(
            id = getString("id")
                ?.takeIf { it.isNotBlank() }
                ?: id,
            missionId = missionId,
            registeredByUserId = registeredByUserId,
            groupId = getString("groupId"),
            groupColorHex = getString("groupColorHex"),
            personName = getString("personName")
                ?.takeIf { it.isNotBlank() },
            ageGroup = ageGroup,
            phoneDigits = getString("phoneDigits")
                ?.filter(Char::isDigit)
                ?.takeIf { it.isNotBlank() },
            address = getString("address")
                ?.takeIf { it.isNotBlank() },
            isAtPersonHome = getBoolean("isAtPersonHome") ?: false,
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            performedActivities = performedActivities,
            bibleStudyStatus = bibleStudyStatus,
            materials = materials,
            surveyAnswers = surveyAnswers,
            acceptedFollowUp = getBoolean("acceptedFollowUp") ?: false,
            notes = getString("notes")
                ?.takeIf { it.isNotBlank() },
            createdAt = getTimestamp("createdAt")?.toLocalDateTime(),
            updatedAt = getTimestamp("updatedAt")?.toLocalDateTime()
        )
    }

    private fun Timestamp.toLocalDateTime(): LocalDateTime =
        Instant
            .ofEpochSecond(
                seconds,
                nanoseconds.toLong()
            )
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
}
