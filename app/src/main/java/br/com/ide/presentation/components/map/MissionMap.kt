package br.com.ide.presentation.components.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.ide.domain.model.MissionCoordinate
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap

@Composable
fun MissionMap(
    latitude: Double = -1.2939,
    longitude: Double = -47.9260,
    zoom: Double = 13.0,

    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null,

    polygonPoints: List<MissionCoordinate> =
        emptyList(),

    isDarkTheme: Boolean,

    onMapClick: (
        latitude: Double,
        longitude: Double
    ) -> Unit = { _, _ -> },

    onInteractionChanged: (
        isInteracting: Boolean
    ) -> Unit = {},

    modifier: Modifier = Modifier,

    recenterKey: Any? = null
) {

    val context =
        LocalContext.current

    val lifecycleOwner =
        LocalLifecycleOwner.current

    val currentOnMapClick by
    rememberUpdatedState(
        onMapClick
    )

    val currentOnInteractionChanged by
    rememberUpdatedState(
        onInteractionChanged
    )

    val styleUrl =
        if (
            isDarkTheme
        ) {
            "https://tiles.openfreemap.org/styles/dark"
        } else {
            "https://tiles.openfreemap.org/styles/liberty"
        }

    val mapView =
        remember {

            MapView(
                context
            ).apply {

                onCreate(
                    null
                )
            }
        }

    val mapInitialized =
        remember {
            booleanArrayOf(
                false
            )
        }

    val polygonPointIcon =
        remember(
            context
        ) {

            createPolygonPointIcon(
                context
            )
        }

    val areaStrokeColor =
        Color.rgb(
            46,
            196,
            182
        )

    val areaFillColor =
        Color.argb(
            65,
            46,
            196,
            182
        )

    // =========================================================
    // Desenho dos elementos
    // =========================================================

    fun redrawMapElements(
        map: MapLibreMap
    ) {

        map.removeAnnotations()

        if (
            selectedLatitude != null &&
            selectedLongitude != null
        ) {

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

        if (
            polygonPoints.isEmpty()
        ) {
            return
        }

        val positions =
            polygonPoints
                .map { point ->

                    LatLng(
                        point.latitude,
                        point.longitude
                    )
                }

        positions
            .forEach { position ->

                map.addMarker(
                    MarkerOptions()
                        .position(
                            position
                        )
                        .icon(
                            polygonPointIcon
                        )
                )
            }

        if (
            positions.size ==
            2
        ) {

            map.addPolyline(
                PolylineOptions()
                    .addAll(
                        positions
                    )
                    .color(
                        areaStrokeColor
                    )
                    .width(
                        5f
                    )
            )

            return
        }

        if (
            positions.size >=
            3
        ) {

            map.addPolygon(
                PolygonOptions()
                    .addAll(
                        positions
                    )
                    .strokeColor(
                        areaStrokeColor
                    )
                    .fillColor(
                        areaFillColor
                    )
            )
        }
    }

    // =========================================================
    // Centralização da área
    // =========================================================

    fun centerArea(
        map: MapLibreMap,
        animated: Boolean
    ) {

        if (
            polygonPoints.isEmpty()
        ) {
            return
        }

        if (
            polygonPoints.size ==
            1
        ) {

            val point =
                polygonPoints.first()

            val update =
                CameraUpdateFactory
                    .newCameraPosition(
                        CameraPosition
                            .Builder()
                            .target(
                                LatLng(
                                    point.latitude,
                                    point.longitude
                                )
                            )
                            .zoom(
                                16.0
                            )
                            .build()
                    )

            if (
                animated
            ) {

                map.animateCamera(
                    update,
                    500
                )

            } else {

                map.moveCamera(
                    update
                )
            }

            return
        }

        val boundsBuilder =
            LatLngBounds.Builder()

        polygonPoints
            .forEach { point ->

                boundsBuilder.include(
                    LatLng(
                        point.latitude,
                        point.longitude
                    )
                )
            }

        val bounds =
            boundsBuilder.build()

        val update =
            CameraUpdateFactory
                .newLatLngBounds(
                    bounds,
                    90
                )

        if (
            animated
        ) {

            map.animateCamera(
                update,
                500
            )

        } else {

            map.moveCamera(
                update
            )
        }
    }

    // =========================================================
    // Lifecycle
    // =========================================================

    DisposableEffect(
        lifecycleOwner,
        mapView
    ) {

        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                when (
                    event
                ) {

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

                    else -> Unit
                }
            }

        lifecycleOwner
            .lifecycle
            .addObserver(
                observer
            )

        onDispose {

            currentOnInteractionChanged(
                false
            )

            lifecycleOwner
                .lifecycle
                .removeObserver(
                    observer
                )

            mapView.onDestroy()
        }
    }

    // =========================================================
    // Estilo
    // =========================================================

    LaunchedEffect(
        styleUrl,
        mapView
    ) {

        mapView.getMapAsync { map ->

            val previousCamera =
                if (
                    mapInitialized[0]
                ) {
                    map.cameraPosition
                } else {
                    null
                }

            map.setStyle(
                styleUrl
            ) {

                redrawMapElements(
                    map
                )

                if (
                    previousCamera != null
                ) {

                    map.cameraPosition =
                        previousCamera

                } else if (
                    polygonPoints.isNotEmpty()
                ) {

                    centerArea(
                        map =
                            map,
                        animated =
                            false
                    )

                } else {

                    map.cameraPosition =
                        CameraPosition
                            .Builder()
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
                }

                mapInitialized[0] =
                    true
            }
        }
    }

    // =========================================================
    // Atualização dos elementos
    // =========================================================

    LaunchedEffect(
        polygonPoints,
        selectedLatitude,
        selectedLongitude,
        mapView
    ) {

        mapView.getMapAsync { map ->

            redrawMapElements(
                map
            )
        }
    }

    // =========================================================
    // Recentrar marcador único
    // =========================================================

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
                    CameraUpdateFactory
                        .newCameraPosition(
                            CameraPosition
                                .Builder()
                                .target(
                                    LatLng(
                                        selectedLatitude,
                                        selectedLongitude
                                    )
                                )
                                .zoom(
                                    16.0
                                )
                                .build()
                        ),
                    700
                )
            }
        }
    }

    // =========================================================
    // MapView
    // =========================================================

    AndroidView(
        modifier =
            modifier
                .fillMaxWidth()
                .height(
                    400.dp
                ),

        factory = {

            mapView.apply {

                setOnTouchListener {
                        view,
                        event ->

                    when (
                        event.actionMasked
                    ) {

                        MotionEvent.ACTION_DOWN -> {

                            view.parent
                                ?.requestDisallowInterceptTouchEvent(
                                    true
                                )

                            currentOnInteractionChanged(
                                true
                            )
                        }

                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> {

                            view.parent
                                ?.requestDisallowInterceptTouchEvent(
                                    false
                                )

                            currentOnInteractionChanged(
                                false
                            )
                        }
                    }

                    /*
                     * false é intencional:
                     * o gesto continua sendo processado
                     * normalmente pelo próprio MapView.
                     */
                    false
                }

                getMapAsync { map ->

                    map.addOnMapClickListener { point ->

                        currentOnMapClick(
                            point.latitude,
                            point.longitude
                        )

                        true
                    }
                }
            }
        },

        update = {
            /*
             * Os elementos são atualizados pelos
             * LaunchedEffects acima.
             */
        }
    )
}

// =============================================================
// Ícone dos vértices
// =============================================================

private fun createPolygonPointIcon(
    context: android.content.Context
): Icon {

    val density =
        context
            .resources
            .displayMetrics
            .density

    val size =
        (
                18f *
                        density
                )
            .toInt()
            .coerceAtLeast(
                18
            )

    val bitmap =
        Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )

    val canvas =
        Canvas(
            bitmap
        )

    val center =
        size /
                2f

    val outerRadius =
        size *
                0.48f

    val innerRadius =
        size *
                0.34f

    val outerPaint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {

            color =
                Color.WHITE

            style =
                Paint.Style.FILL
        }

    val innerPaint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {

            color =
                Color.rgb(
                    46,
                    196,
                    182
                )

            style =
                Paint.Style.FILL
        }

    canvas.drawCircle(
        center,
        center,
        outerRadius,
        outerPaint
    )

    canvas.drawCircle(
        center,
        center,
        innerRadius,
        innerPaint
    )

    return IconFactory
        .getInstance(
            context
        )
        .fromBitmap(
            bitmap
        )
}
