package br.com.ide.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import br.com.ide.domain.location.MissionLocationTracker
import br.com.ide.domain.model.MissionLocationUpdate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class AndroidMissionLocationTracker @Inject constructor(
    @ApplicationContext
    private val context: Context
) : MissionLocationTracker {

    private val locationManager =
        context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

    override fun observeLocation():
            Flow<MissionLocationUpdate> {

        return callbackFlow {

            if (
                !hasLocationPermission()
            ) {
                close(
                    SecurityException(
                        "Location permission is required."
                    )
                )
                return@callbackFlow
            }

            val listener =
                object : LocationListener {

                    override fun onLocationChanged(
                        location: Location
                    ) {

                        trySend(
                            MissionLocationUpdate(
                                latitude =
                                    location.latitude,
                                longitude =
                                    location.longitude,
                                accuracyMeters =
                                    if (
                                        location.hasAccuracy()
                                    ) {
                                        location.accuracy
                                    } else {
                                        null
                                    }
                            )
                        )
                    }

                    @Deprecated(
                        "Required by older Android versions."
                    )
                    override fun onStatusChanged(
                        provider: String?,
                        status: Int,
                        extras: Bundle?
                    ) = Unit
                }

            startUpdates(
                listener =
                    listener
            )

            awaitClose {

                locationManager
                    .removeUpdates(
                        listener
                    )
            }
        }
    }

    @SuppressLint(
        "MissingPermission"
    )
    private fun startUpdates(
        listener: LocationListener
    ) {

        /*
         * Emitimos uma localização já conhecida primeiro,
         * quando disponível, para o marcador aparecer sem
         * precisar aguardar o próximo fix do GPS.
         */
        bestLastKnownLocation()
            ?.let(
                listener::onLocationChanged
            )

        if (
            locationManager
                .isProviderEnabled(
                    LocationManager.GPS_PROVIDER
                )
        ) {

            locationManager
                .requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL_MS,
                    LOCATION_MIN_DISTANCE_METERS,
                    listener,
                    Looper.getMainLooper()
                )
        }

        if (
            locationManager
                .isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )
        ) {

            locationManager
                .requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL_MS,
                    LOCATION_MIN_DISTANCE_METERS,
                    listener,
                    Looper.getMainLooper()
                )
        }
    }

    @SuppressLint(
        "MissingPermission"
    )
    private fun bestLastKnownLocation():
            Location? {

        if (
            !hasLocationPermission()
        ) {
            return null
        }

        return listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )
            .mapNotNull { provider ->

                runCatching {

                    locationManager
                        .getLastKnownLocation(
                            provider
                        )

                }.getOrNull()
            }
            .maxByOrNull {
                it.time
            }
    }

    private fun hasLocationPermission():
            Boolean {

        val fineGranted =
            ContextCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ) ==
                    PackageManager
                        .PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ) ==
                    PackageManager
                        .PERMISSION_GRANTED

        return fineGranted ||
                coarseGranted
    }

    private companion object {

        const val LOCATION_UPDATE_INTERVAL_MS =
            3_000L

        const val LOCATION_MIN_DISTANCE_METERS =
            3f
    }
}
