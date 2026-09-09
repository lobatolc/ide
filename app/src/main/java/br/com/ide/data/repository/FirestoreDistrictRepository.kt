package br.com.ide.data.repository

import br.com.ide.domain.model.District
import br.com.ide.domain.repository.DistrictRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDistrictRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : DistrictRepository {

    override suspend fun getDistricts():
            Result<List<District>> =
        runCatching {

            firestore
                .collection("districts")
                .whereEqualTo(
                    "active",
                    true
                )
                .get()
                .await()
                .documents
                .map { document ->

                    District(
                        id = document.id,
                        name =
                            document
                                .getString("name")
                                .orEmpty(),
                        active =
                            document.getBoolean(
                                "active"
                            ) ?: true
                    )
                }
                .sortedBy {
                    it.name
                }
        }

    override suspend fun getDistrictById(
        districtId: String
    ): Result<District> =
        runCatching {

            val document =
                firestore
                    .collection("districts")
                    .document(districtId)
                    .get()
                    .await()

            if (!document.exists()) {
                error(
                    "District not found"
                )
            }

            District(
                id = document.id,
                name =
                    document
                        .getString("name")
                        .orEmpty(),
                active =
                    document.getBoolean(
                        "active"
                    ) ?: true
            )
        }
}