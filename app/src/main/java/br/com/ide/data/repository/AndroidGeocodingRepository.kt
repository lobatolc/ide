package br.com.ide.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import br.com.ide.domain.model.GeocodedAddress
import br.com.ide.domain.repository.GeocodingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class AndroidGeocodingRepository @Inject constructor(
    @ApplicationContext
    private val context: Context
) : GeocodingRepository {

    override suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Result<GeocodedAddress> {

        return runCatching {

            val geocoder =
                Geocoder(
                    context,
                    Locale.getDefault()
                )

            val address =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.TIRAMISU
                ) {

                    getAddressModern(
                        geocoder =
                            geocoder,
                        latitude =
                            latitude,
                        longitude =
                            longitude
                    )

                } else {

                    @Suppress("DEPRECATION")
                    geocoder
                        .getFromLocation(
                            latitude,
                            longitude,
                            1
                        )
                        ?.firstOrNull()
                }
                    ?: throw IllegalStateException(
                        "Address not found"
                    )

            val street =
                address.thoroughfare
                    ?.trim()
                    .orEmpty()

            val number =
                address.subThoroughfare
                    ?.trim()
                    .orEmpty()

            val neighborhood =
                address.subLocality
                    ?.trim()
                    .orEmpty()

            val city =
                address.locality
                    ?.trim()
                    .orEmpty()

            val state =
                address.adminArea
                    ?.trim()
                    .orEmpty()

            val mainName =
                listOf(
                    street,
                    number
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .joinToString(
                        ", "
                    )
                    .ifBlank {
                        address.featureName
                            ?.trim()
                            .orEmpty()
                    }

            val secondaryAddress =
                listOf(
                    neighborhood,
                    city,
                    state
                )
                    .filter {
                        it.isNotBlank()
                    }
                    .distinct()
                    .joinToString(
                        ", "
                    )

            GeocodedAddress(
                name =
                    mainName,
                address =
                    secondaryAddress
            )
        }
    }

    private suspend fun getAddressModern(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double
    ): Address? {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.TIRAMISU
        ) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->

            geocoder.getFromLocation(
                latitude,
                longitude,
                1
            ) { addresses ->

                if (
                    continuation.isActive
                ) {

                    continuation.resume(
                        addresses.firstOrNull()
                    )
                }
            }
        }
    }
}