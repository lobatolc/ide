package br.com.ide.domain.util

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object GroupColorGenerator {

    private const val GOLDEN_ANGLE = 137.508

    private const val SATURATION = 0.72
    private const val LIGHTNESS = 0.52

    private const val MIN_HUE_DISTANCE = 28.0

    fun generate(
        existingColors: List<String>
    ): String {

        val existingHues =
            existingColors
                .mapNotNull {
                    hexToHsl(it)
                }
                .map {
                    it.hue
                }

        var index =
            existingColors.size

        repeat(720) {

            val hue =
                normalizeHue(
                    index * GOLDEN_ANGLE
                )

            val isDistinct =
                existingHues.all { existingHue ->

                    hueDistance(
                        hue,
                        existingHue
                    ) >= MIN_HUE_DISTANCE
                }

            if (
                isDistinct
            ) {

                return hslToHex(
                    hue =
                        hue,

                    saturation =
                        SATURATION,

                    lightness =
                        LIGHTNESS
                )
            }

            index++
        }

        // Fallback extremamente improvável.
        return hslToHex(
            hue =
                normalizeHue(
                    existingColors.size *
                            GOLDEN_ANGLE
                ),
            saturation =
                SATURATION,
            lightness =
                LIGHTNESS
        )
    }

    private fun hueDistance(
        hueA: Double,
        hueB: Double
    ): Double {

        val difference =
            abs(
                hueA -
                        hueB
            )

        return min(
            difference,
            360.0 - difference
        )
    }

    private fun normalizeHue(
        hue: Double
    ): Double {

        return (
                (hue % 360.0) +
                        360.0
                ) % 360.0
    }

    private fun hslToHex(
        hue: Double,
        saturation: Double,
        lightness: Double
    ): String {

        val chroma =
            (
                    1.0 -
                            abs(
                                2.0 * lightness -
                                        1.0
                            )
                    ) *
                    saturation

        val hueSegment =
            hue /
                    60.0

        val x =
            chroma *
                    (
                            1.0 -
                                    abs(
                                        hueSegment %
                                                2.0 -
                                                1.0
                                    )
                            )

        val (
            redPrime,
            greenPrime,
            bluePrime
        ) =
            when {

                hueSegment < 1.0 ->
                    Triple(
                        chroma,
                        x,
                        0.0
                    )

                hueSegment < 2.0 ->
                    Triple(
                        x,
                        chroma,
                        0.0
                    )

                hueSegment < 3.0 ->
                    Triple(
                        0.0,
                        chroma,
                        x
                    )

                hueSegment < 4.0 ->
                    Triple(
                        0.0,
                        x,
                        chroma
                    )

                hueSegment < 5.0 ->
                    Triple(
                        x,
                        0.0,
                        chroma
                    )

                else ->
                    Triple(
                        chroma,
                        0.0,
                        x
                    )
            }

        val match =
            lightness -
                    chroma /
                    2.0

        val red =
            ((redPrime + match) * 255.0)
                .toInt()
                .coerceIn(
                    0,
                    255
                )

        val green =
            ((greenPrime + match) * 255.0)
                .toInt()
                .coerceIn(
                    0,
                    255
                )

        val blue =
            ((bluePrime + match) * 255.0)
                .toInt()
                .coerceIn(
                    0,
                    255
                )

        return String.format(
            "#%02X%02X%02X",
            red,
            green,
            blue
        )
    }

    private fun hexToHsl(
        hex: String
    ): HslColor? {

        val normalizedHex =
            hex
                .removePrefix(
                    "#"
                )
                .trim()

        if (
            normalizedHex.length != 6
        ) {
            return null
        }

        val red =
            normalizedHex
                .substring(
                    0,
                    2
                )
                .toIntOrNull(
                    16
                )
                ?: return null

        val green =
            normalizedHex
                .substring(
                    2,
                    4
                )
                .toIntOrNull(
                    16
                )
                ?: return null

        val blue =
            normalizedHex
                .substring(
                    4,
                    6
                )
                .toIntOrNull(
                    16
                )
                ?: return null

        val redNormalized =
            red /
                    255.0

        val greenNormalized =
            green /
                    255.0

        val blueNormalized =
            blue /
                    255.0

        val maxValue =
            max(
                redNormalized,
                max(
                    greenNormalized,
                    blueNormalized
                )
            )

        val minValue =
            min(
                redNormalized,
                min(
                    greenNormalized,
                    blueNormalized
                )
            )

        val delta =
            maxValue -
                    minValue

        val lightness =
            (
                    maxValue +
                            minValue
                    ) /
                    2.0

        val saturation =
            if (
                delta == 0.0
            ) {
                0.0
            } else {
                delta /
                        (
                                1.0 -
                                        abs(
                                            2.0 * lightness -
                                                    1.0
                                        )
                                )
            }

        val hue =
            when {

                delta == 0.0 ->
                    0.0

                maxValue ==
                        redNormalized -> {

                    60.0 *
                            (
                                    (
                                            (
                                                    greenNormalized -
                                                            blueNormalized
                                                    ) /
                                                    delta
                                            ) %
                                            6.0
                                    )
                }

                maxValue ==
                        greenNormalized -> {

                    60.0 *
                            (
                                    (
                                            blueNormalized -
                                                    redNormalized
                                            ) /
                                            delta +
                                            2.0
                                    )
                }

                else -> {

                    60.0 *
                            (
                                    (
                                            redNormalized -
                                                    greenNormalized
                                            ) /
                                            delta +
                                            4.0
                                    )
                }
            }

        return HslColor(
            hue =
                normalizeHue(
                    hue
                ),
            saturation =
                saturation,
            lightness =
                lightness
        )
    }

    private data class HslColor(
        val hue: Double,
        val saturation: Double,
        val lightness: Double
    )
}