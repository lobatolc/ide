package br.com.ide.data.repository

import br.com.ide.domain.model.Church
import br.com.ide.domain.repository.ChurchRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreChurchRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChurchRepository {

    override suspend fun getChurches():
            Result<List<Church>> =
        runCatching {

            firestore
                .collection("churches")
                .whereEqualTo(
                    "active",
                    true
                )
                .get()
                .await()
                .documents
                .map {
                    it.toChurch()
                }
                .sortedBy {
                    it.name
                }
        }

    override suspend fun getChurchesByDistrict(
        districtId: String
    ): Result<List<Church>> =
        runCatching {

            firestore
                .collection("churches")
                .whereEqualTo(
                    "districtId",
                    districtId
                )
                .whereEqualTo(
                    "active",
                    true
                )
                .get()
                .await()
                .documents
                .map {
                    it.toChurch()
                }
                .sortedBy {
                    it.name
                }
        }

    override suspend fun getChurchById(
        churchId: String
    ): Result<Church> =
        runCatching {

            val document =
                firestore
                    .collection("churches")
                    .document(churchId)
                    .get()
                    .await()

            if (!document.exists()) {
                error(
                    "Church not found"
                )
            }

            document.toChurch()
        }

    private fun
            com.google.firebase.firestore.DocumentSnapshot
            .toChurch(): Church {

        return Church(
            id = id,

            name =
                getString("name")
                    .orEmpty(),

            districtId =
                getString("districtId")
                    .orEmpty(),

            active =
                getBoolean("active")
                    ?: true,

            address =
                getString("address"),

            latitude =
                getDouble("latitude"),

            longitude =
                getDouble("longitude")
        )
    }
}