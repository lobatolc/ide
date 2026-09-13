package br.com.ide.presentation.components.map

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

@Composable
fun MissionMap(
    latitude: Double = -1.2939,
    longitude: Double = -47.9260,
    zoom: Double = 13.0,
    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null,
    isDarkTheme: Boolean,
    onMapClick: (
        latitude: Double,
        longitude: Double
    ) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    recenterKey: Any? = null,
) {
    val context =
        LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    val styleUrl =
        if (isDarkTheme) {
            "https://tiles.openfreemap.org/styles/dark"
        } else {
            "https://tiles.openfreemap.org/styles/liberty"
        }

    val mapView =
        remember {
            MapView(context).apply {
                onCreate(null)
            }
        }

    val markerHolder =
        remember {
            arrayOfNulls<Marker>(1)
        }

    val mapInitialized =
        remember {
            booleanArrayOf(false)
        }

    /*
     * Lifecycle do MapView.
     */
    DisposableEffect(
        lifecycleOwner,
        mapView
    ) {
        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                when (event) {

                    Lifecycle.Event.ON_START -> {
                        mapView.onStart()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        mapView.onResume()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        mapView.onPause()
                    }

                    Lifecycle.Event.ON_STOP -> {
                        mapView.onStop()
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        mapView.onDestroy()
                    }

                    else -> Unit
                }
            }

        lifecycleOwner
            .lifecycle
            .addObserver(
                observer
            )

        onDispose {

            lifecycleOwner
                .lifecycle
                .removeObserver(
                    observer
                )

            mapView.onDestroy()
        }
    }

    /*
     * O estilo só é recarregado quando styleUrl muda.
     *
     * Isso acontece, por exemplo, quando o usuário
     * muda entre tema claro e escuro.
     *
     * Preservamos a câmera atual para não "pular"
     * para outro lugar depois da troca de tema.
     */
    LaunchedEffect(
        styleUrl,
        mapView
    ) {
        mapView.getMapAsync { map ->

            val previousCamera =
                if (mapInitialized[0]) {
                    map.cameraPosition
                } else {
                    null
                }

            map.setStyle(
                styleUrl
            ) {

                if (
                    previousCamera != null
                ) {

                    map.cameraPosition =
                        previousCamera

                } else {

                    map.cameraPosition =
                        CameraPosition.Builder()
                            .target(
                                LatLng(
                                    latitude,
                                    longitude
                                )
                            )
                            .zoom(
                                zoom
                            )
                            .build()

                    mapInitialized[0] =
                        true
                }

                /*
                 * Como trocar o estilo remove
                 * elementos adicionados ao mapa,
                 * recriamos o marcador.
                 */
                markerHolder[0] =
                    null

                if (
                    selectedLatitude != null &&
                    selectedLongitude != null
                ) {

                    markerHolder[0] =
                        map.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        selectedLatitude,
                                        selectedLongitude
                                    )
                                )
                        )
                }
            }
        }
    }

    LaunchedEffect(
        recenterKey
    ) {
        if (
            recenterKey != null &&
            selectedLatitude != null &&
            selectedLongitude != null
        ) {

            mapView.getMapAsync { map ->

                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(
                                LatLng(
                                    selectedLatitude,
                                    selectedLongitude
                                )
                            )
                            .zoom(16.0)
                            .build()
                    ),
                    700
                )
            }
        }
    }

    AndroidView(
        modifier =
            modifier
                .fillMaxWidth()
                .height(400.dp),

        factory = {

            mapView.apply {

                getMapAsync { map ->

                    map.addOnMapClickListener { point ->

                        onMapClick(
                            point.latitude,
                            point.longitude
                        )

                        true
                    }
                }
            }
        },

        /*
         * Em recomposições normais não recarregamos
         * o estilo. Apenas atualizamos o marcador.
         */
        update = {

            it.getMapAsync { map ->

                if (
                    selectedLatitude == null ||
                    selectedLongitude == null
                ) {

                    markerHolder[0]
                        ?.let { marker ->

                            map.removeMarker(
                                marker
                            )
                        }

                    markerHolder[0] =
                        null

                    return@getMapAsync
                }

                val position =
                    LatLng(
                        selectedLatitude,
                        selectedLongitude
                    )

                val marker =
                    markerHolder[0]

                if (
                    marker == null
                ) {

                    markerHolder[0] =
                        map.addMarker(
                            MarkerOptions()
                                .position(
                                    position
                                )
                        )

                } else {

                    marker.position =
                        position
                }
            }
        }
    )
}