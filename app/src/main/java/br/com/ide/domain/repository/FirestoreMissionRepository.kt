package br.com.ide.data.repository

import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionAction
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.repository.MissionRepository
import com.google.firebase.Timestamp
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

                val scheduledTimestamp =
                    document.getTimestamp("scheduledAt")
                        ?: return@mapNotNull null

                val statusName =
                    document.getString("status")
                        ?: return@mapNotNull null

                val status =
                    runCatching {
                        MissionStatus.valueOf(statusName)
                    }.getOrNull()
                        ?: return@mapNotNull null

                val actions =
                    (document.get("actions") as? List<*>)
                        ?.mapNotNull { item ->

                            val action =
                                item as? Map<*, *>
                                    ?: return@mapNotNull null

                            val id =
                                action["id"] as? String
                                    ?: return@mapNotNull null

                            val name =
                                action["name"] as? String
                                    ?: return@mapNotNull null

                            MissionAction(
                                id = id,
                                name = name
                            )
                        }
                        ?: emptyList()

                Mission(
                    id = document.id,
                    name = document.getString("name").orEmpty(),
                    scheduledAt =
                        scheduledTimestamp.toLocalDateTime(),
                    address =
                        document.getString("address").orEmpty(),
                    description =
                        document.getString("description").orEmpty(),
                    status = status,
                    photoUrl =
                        document.getString("photoUrl"),
                    actions = actions,
                    totalDurationMinutes =
                        document.getLong(
                            "totalDurationMinutes"
                        ),
                    createdBy =
                        document.getString("createdBy").orEmpty()
                )
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

        if (!document.exists()) {
            return null
        }

        val scheduledTimestamp =
            document.getTimestamp("scheduledAt")
                ?: return null

        val statusName =
            document.getString("status")
                ?: return null

        val status =
            runCatching {
                MissionStatus.valueOf(statusName)
            }.getOrNull()
                ?: return null

        val actions =
            (document.get("actions") as? List<*>)
                ?.mapNotNull { item ->

                    val action =
                        item as? Map<*, *>
                            ?: return@mapNotNull null

                    val id =
                        action["id"] as? String
                            ?: return@mapNotNull null

                    val name =
                        action["name"] as? String
                            ?: return@mapNotNull null

                    MissionAction(
                        id = id,
                        name = name
                    )
                }
                ?: emptyList()

        return Mission(
            id = document.id,
            name = document.getString("name").orEmpty(),
            scheduledAt =
                scheduledTimestamp.toLocalDateTime(),
            address =
                document.getString("address").orEmpty(),
            description =
                document.getString("description").orEmpty(),
            status = status,
            photoUrl =
                document.getString("photoUrl"),
            actions = actions,
            totalDurationMinutes =
                document.getLong(
                    "totalDurationMinutes"
                ),
            createdBy =
                document.getString("createdBy").orEmpty()
        )
    }

    override suspend fun createMission(
        mission: Mission
    ): Result<Unit> {
        return try {

            val document =
                if (mission.id.isBlank()) {
                    firestore
                        .collection("missions")
                        .document()
                } else {
                    firestore
                        .collection("missions")
                        .document(mission.id)
                }

            document
                .set(
                    mission.toFirestoreMap()
                )
                .await()

            Result.success(Unit)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun updateMission(
        mission: Mission
    ): Result<Unit> {
        return try {

            firestore
                .collection("missions")
                .document(mission.id)
                .set(
                    mission.toFirestoreMap()
                )
                .await()

            Result.success(Unit)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun Mission.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "scheduledAt" to scheduledAt.toTimestamp(),
            "address" to address,
            "description" to description,
            "status" to status.name,
            "photoUrl" to photoUrl,
            "actions" to actions.map { action ->
                mapOf(
                    "id" to action.id,
                    "name" to action.name
                )
            },
            "totalDurationMinutes" to
                    totalDurationMinutes,
            "createdBy" to createdBy
        )
    }

    private fun LocalDateTime.toTimestamp(): Timestamp {
        val instant =
            atZone(
                ZoneId.systemDefault()
            ).toInstant()

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