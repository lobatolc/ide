package br.com.ide.data.repository

import br.com.ide.domain.model.MissionTrackPoint
import br.com.ide.domain.repository.MissionTrackRemoteRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreMissionTrackRemoteRepository @Inject constructor(
    private val firestore:
    FirebaseFirestore
) : MissionTrackRemoteRepository {

    override suspend fun save(
        point: MissionTrackPoint
    ): Result<Unit> {

        return runCatching {

            val data =
                mapOf(
                    "id" to
                            point.id,
                    "missionId" to
                            point.missionId,
                    "userId" to
                            point.userId,
                    "latitude" to
                            point.latitude,
                    "longitude" to
                            point.longitude,
                    "accuracyMeters" to
                            point.accuracyMeters,
                    "capturedAt" to
                            Timestamp(
                                Date(
                                    point
                                        .capturedAtEpochMillis
                                )
                            ),
                    "uploadedAt" to
                            FieldValue
                                .serverTimestamp()
                )

            firestore
                .collection(
                    "missions"
                )
                .document(
                    point.missionId
                )
                .collection(
                    "participants"
                )
                .document(
                    point.userId
                )
                .collection(
                    "track"
                )
                .document(
                    point.id
                )
                .set(
                    data
                )
                .await()

            Unit
        }
    }

    override fun observeTrack(
        missionId: String,
        userId: String
    ): Flow<List<MissionTrackPoint>> {

        return callbackFlow {

            var registration:
                    ListenerRegistration? =
                null

            registration =
                firestore
                    .collection(
                        "missions"
                    )
                    .document(
                        missionId
                    )
                    .collection(
                        "participants"
                    )
                    .document(
                        userId
                    )
                    .collection(
                        "track"
                    )
                    .orderBy(
                        "capturedAt"
                    )
                    .addSnapshotListener {
                            snapshot,
                            error ->

                        if (
                            error != null
                        ) {
                            close(
                                error
                            )
                            return@addSnapshotListener
                        }

                        val points =
                            snapshot
                                ?.documents
                                ?.mapNotNull { document ->

                                    val latitude =
                                        document
                                            .getDouble(
                                                "latitude"
                                            )
                                            ?: return@mapNotNull null

                                    val longitude =
                                        document
                                            .getDouble(
                                                "longitude"
                                            )
                                            ?: return@mapNotNull null

                                    val capturedAt =
                                        document
                                            .getTimestamp(
                                                "capturedAt"
                                            )
                                            ?.toDate()
                                            ?.time
                                            ?: return@mapNotNull null

                                    MissionTrackPoint(
                                        id =
                                            document
                                                .getString(
                                                    "id"
                                                )
                                                ?: document.id,
                                        missionId =
                                            document
                                                .getString(
                                                    "missionId"
                                                )
                                                ?: missionId,
                                        userId =
                                            document
                                                .getString(
                                                    "userId"
                                                )
                                                ?: userId,
                                        latitude =
                                            latitude,
                                        longitude =
                                            longitude,
                                        accuracyMeters =
                                            document
                                                .getDouble(
                                                    "accuracyMeters"
                                                )
                                                ?.toFloat(),
                                        capturedAtEpochMillis =
                                            capturedAt
                                    )
                                }
                                ?: emptyList()

                        trySend(
                            points
                        )
                    }

            awaitClose {

                registration
                    ?.remove()
            }
        }
    }
}
