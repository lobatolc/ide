package br.com.ide.domain.usecase

import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionCoordinate
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.MissionPersonalMetrics
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CalculateMissionPersonalMetricsUseCase
@Inject constructor() {

    operator fun invoke(
        currentUserId: String,
        encounters: List<MissionEncounter>,
        participant: MissionParticipantState?,
        trackPoints: List<MissionCoordinate>,
        now: LocalDateTime =
            LocalDateTime.now()
    ): MissionPersonalMetrics {

        val userEncounters =
            encounters
                .filter {
                    it.registeredByUserId ==
                            currentUserId
                }

        val visitCount =
            userEncounters
                .count {
                    MissionActivityType.VISIT in
                            it.performedActivities
                }

        val prayerCount =
            userEncounters
                .count {
                    MissionActivityType.PRAYER in
                            it.performedActivities
                }

        val customActivityCount =
            userEncounters
                .count {
                    MissionActivityType.OTHER in
                            it.performedActivities
                }

        val offeredBibleStudyCount =
            userEncounters
                .count {
                    it.bibleStudyStatus !=
                            BibleStudyStatus.NOT_OFFERED
                }

        val acceptedBibleStudyCount =
            userEncounters
                .count {
                    it.bibleStudyStatus ==
                            BibleStudyStatus.ACCEPTED
                }

        val deliveredMaterialCount =
            userEncounters
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
         * No fluxo de Novo encontro, OPINION_SURVEY só entra em
         * performedActivities quando todas as perguntas da pesquisa
         * foram respondidas. Portanto este marcador representa uma
         * pesquisa concluída.
         */
        val completedSurveyCount =
            userEncounters
                .count {
                    MissionActivityType
                        .OPINION_SURVEY in
                            it.performedActivities
                }

        val distanceMeters =
            calculateDistanceMeters(
                points =
                    trackPoints
            )

        val participationSeconds =
            calculateParticipationSeconds(
                currentUserId =
                    currentUserId,
                participant =
                    participant,
                now =
                    now
            )

        return MissionPersonalMetrics(
            encounterCount =
                userEncounters.size,
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
        currentUserId: String,
        participant: MissionParticipantState?,
        now: LocalDateTime
    ): Long {

        if (
            participant == null ||
            participant.userId !=
            currentUserId
        ) {
            return 0L
        }

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
        points: List<MissionCoordinate>
    ): Double {

        if (
            points.size <
            2
        ) {
            return 0.0
        }

        var totalMeters =
            0.0

        points
            .zipWithNext()
            .forEach {
                    (
                        first,
                        second
                    ) ->

                if (
                    !first.isValidCoordinate() ||
                    !second.isValidCoordinate()
                ) {
                    return@forEach
                }

                totalMeters +=
                    distanceBetweenMeters(
                        first =
                            first,
                        second =
                            second
                    )
            }

        return totalMeters
            .coerceAtLeast(
                0.0
            )
    }

    private fun distanceBetweenMeters(
        first: MissionCoordinate,
        second: MissionCoordinate
    ): Double {

        val latitude1 =
            Math.toRadians(
                first.latitude
            )

        val latitude2 =
            Math.toRadians(
                second.latitude
            )

        val latitudeDelta =
            Math.toRadians(
                second.latitude -
                        first.latitude
            )

        val longitudeDelta =
            Math.toRadians(
                second.longitude -
                        first.longitude
            )

        val haversine =
            sin(
                latitudeDelta /
                        2.0
            )
                .let {
                    it * it
                } +
                    cos(
                        latitude1
                    ) *
                    cos(
                        latitude2
                    ) *
                    sin(
                        longitudeDelta /
                                2.0
                    )
                        .let {
                            it * it
                        }

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

    private fun MissionCoordinate
            .isValidCoordinate():
            Boolean {

        return latitude in
                -90.0..90.0 &&
                longitude in
                -180.0..180.0
    }

    private companion object {

        const val EARTH_RADIUS_METERS =
            6_371_000.0
    }
}
