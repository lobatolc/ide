package br.com.ide.domain.usecase

import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionGeneralMetrics
import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.MissionTrackPoint
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CalculateMissionGeneralMetricsUseCase
@Inject constructor() {

    operator fun invoke(
        participants: List<MissionParticipantState>,
        encounters: List<MissionEncounter>,
        trackPointsByUser:
        Map<String, List<MissionTrackPoint>>,
        groupCount: Int,
        now: LocalDateTime =
            LocalDateTime.now()
    ): MissionGeneralMetrics {

        val participantIds =
            participants
                .map {
                    it.userId
                }
                .toSet()

        val visitCount =
            encounters
                .count {
                    MissionActivityType.VISIT in
                            it.performedActivities
                }

        val prayerCount =
            encounters
                .count {
                    MissionActivityType.PRAYER in
                            it.performedActivities
                }

        val customActivityCount =
            encounters
                .count {
                    MissionActivityType.OTHER in
                            it.performedActivities
                }

        val offeredBibleStudyCount =
            encounters
                .count {
                    it.bibleStudyStatus !=
                            BibleStudyStatus.NOT_OFFERED
                }

        val acceptedBibleStudyCount =
            encounters
                .count {
                    it.bibleStudyStatus ==
                            BibleStudyStatus.ACCEPTED
                }

        val deliveredMaterialCount =
            encounters
                .sumOf { encounter ->

                    encounter
                        .materials
                        .sumOf { material ->

                            material
                                .quantity
                                .coerceAtLeast(
                                    0
                                )
                        }
                }

        /*
         * No cadastro de encontro, OPINION_SURVEY só é salvo em
         * performedActivities quando todas as perguntas configuradas
         * foram respondidas.
         */
        val completedSurveyCount =
            encounters
                .count {
                    MissionActivityType
                        .OPINION_SURVEY in
                            it.performedActivities
                }

        val distanceMeters =
            trackPointsByUser
                .asSequence()
                .filter {
                        (
                            userId,
                            _
                        ) ->

                    userId in
                            participantIds
                }
                .sumOf {
                        (
                            _,
                            points
                        ) ->

                    calculateDistanceMeters(
                        points =
                            points
                    )
                }

        val participationSeconds =
            participants
                .sumOf { participant ->

                    calculateParticipationSeconds(
                        participant =
                            participant,
                        now =
                            now
                    )
                }

        return MissionGeneralMetrics(
            participantCount =
                participants
                    .map {
                        it.userId
                    }
                    .distinct()
                    .size,
            groupCount =
                groupCount
                    .coerceAtLeast(
                        0
                    ),
            encounterCount =
                encounters.size,
            visitCount =
                visitCount,
            prayerCount =
                prayerCount,
            customActivityCount =
                customActivityCount,
            offeredBibleStudyCount =
                offeredBibleStudyCount,
            acceptedBibleStudyCount =
                acceptedBibleStudyCount,
            deliveredMaterialCount =
                deliveredMaterialCount,
            completedSurveyCount =
                completedSurveyCount,
            distanceMeters =
                distanceMeters,
            participationSeconds =
                participationSeconds
        )
    }

    private fun calculateParticipationSeconds(
        participant: MissionParticipantState,
        now: LocalDateTime
    ): Long {

        val joinedAt =
            participant.joinedAt
                ?: return 0L

        val finishedAt =
            participant.endedAt
                ?: now

        return Duration
            .between(
                joinedAt,
                finishedAt
            )
            .seconds
            .coerceAtLeast(
                0L
            )
    }

    private fun calculateDistanceMeters(
        points: List<MissionTrackPoint>
    ): Double {

        val orderedPoints =
            points
                .asSequence()
                .filter {
                    it.latitude in
                            -90.0..90.0 &&
                            it.longitude in
                            -180.0..180.0
                }
                .sortedBy {
                    it.capturedAtEpochMillis
                }
                .toList()

        if (
            orderedPoints.size <
            2
        ) {
            return 0.0
        }

        return orderedPoints
            .zipWithNext()
            .sumOf {
                    (
                        first,
                        second
                    ) ->

                distanceBetweenMeters(
                    firstLatitude =
                        first.latitude,
                    firstLongitude =
                        first.longitude,
                    secondLatitude =
                        second.latitude,
                    secondLongitude =
                        second.longitude
                )
            }
            .coerceAtLeast(
                0.0
            )
    }

    private fun distanceBetweenMeters(
        firstLatitude: Double,
        firstLongitude: Double,
        secondLatitude: Double,
        secondLongitude: Double
    ): Double {

        val latitude1 =
            Math.toRadians(
                firstLatitude
            )

        val latitude2 =
            Math.toRadians(
                secondLatitude
            )

        val latitudeDelta =
            Math.toRadians(
                secondLatitude -
                        firstLatitude
            )

        val longitudeDelta =
            Math.toRadians(
                secondLongitude -
                        firstLongitude
            )

        val latitudeHaversine =
            sin(
                latitudeDelta /
                        2.0
            )

        val longitudeHaversine =
            sin(
                longitudeDelta /
                        2.0
            )

        val haversine =
            latitudeHaversine *
                    latitudeHaversine +
                    cos(
                        latitude1
                    ) *
                    cos(
                        latitude2
                    ) *
                    longitudeHaversine *
                    longitudeHaversine

        val centralAngle =
            2.0 *
                    atan2(
                        sqrt(
                            haversine
                        ),
                        sqrt(
                            1.0 -
                                    haversine
                        )
                    )

        return EARTH_RADIUS_METERS *
                centralAngle
    }

    private companion object {

        const val EARTH_RADIUS_METERS =
            6_371_000.0
    }
}
