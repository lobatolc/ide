package br.com.ide.domain.usecase

import br.com.ide.domain.model.MissionTrackPoint
import br.com.ide.domain.repository.MissionTrackLocalRepository
import java.util.UUID
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RecordMissionTrackPointUseCase @Inject constructor(
    private val localRepository: MissionTrackLocalRepository
) {

    suspend operator fun invoke(
        missionId: String,
        userId: String,
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float?,
        capturedAtEpochMillis: Long = System.currentTimeMillis()
    ): MissionTrackPoint? {

        val latest =
            localRepository.getLatest(
                missionId = missionId,
                userId = userId
            )

        if (
            latest != null &&
            !shouldRecord(
                latest = latest,
                latitude = latitude,
                longitude = longitude,
                capturedAtEpochMillis = capturedAtEpochMillis
            )
        ) {
            return null
        }

        val point =
            MissionTrackPoint(
                id = UUID.randomUUID().toString(),
                missionId = missionId,
                userId = userId,
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = accuracyMeters,
                capturedAtEpochMillis = capturedAtEpochMillis
            )

        localRepository.save(point)

        return point
    }

    private fun shouldRecord(
        latest: MissionTrackPoint,
        latitude: Double,
        longitude: Double,
        capturedAtEpochMillis: Long
    ): Boolean {

        val elapsedMillis =
            (capturedAtEpochMillis - latest.capturedAtEpochMillis)
                .coerceAtLeast(0L)

        val distanceMeters =
            distanceMeters(
                latitudeA = latest.latitude,
                longitudeA = latest.longitude,
                latitudeB = latitude,
                longitudeB = longitude
            )

        return distanceMeters >= MIN_DISTANCE_METERS ||
                elapsedMillis >= MAX_INTERVAL_MILLIS
    }

    private fun distanceMeters(
        latitudeA: Double,
        longitudeA: Double,
        latitudeB: Double,
        longitudeB: Double
    ): Double {

        val earthRadiusMeters = 6_371_000.0
        val latitudeARadians = Math.toRadians(latitudeA)
        val latitudeBRadians = Math.toRadians(latitudeB)
        val deltaLatitude = Math.toRadians(latitudeB - latitudeA)
        val deltaLongitude = Math.toRadians(longitudeB - longitudeA)

        val haversine =
            sin(deltaLatitude / 2.0) *
                    sin(deltaLatitude / 2.0) +
                    cos(latitudeARadians) *
                    cos(latitudeBRadians) *
                    sin(deltaLongitude / 2.0) *
                    sin(deltaLongitude / 2.0)

        val angularDistance =
            2.0 * atan2(
                sqrt(haversine),
                sqrt(1.0 - haversine)
            )

        return earthRadiusMeters * angularDistance
    }

    private companion object {
        const val MIN_DISTANCE_METERS = 5.0
        const val MAX_INTERVAL_MILLIS = 15_000L
    }
}
