package br.com.ide.presentation.components.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import br.com.ide.domain.model.MissionCoordinate
import br.com.ide.domain.model.MissionEncounterMarker
import br.com.ide.domain.model.MissionParticipantStatus
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

@Composable
fun MissionMap(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,

    latitude: Double = -1.2939,
    longitude: Double = -47.9260,
    zoom: Double = 13.0,

    selectedLatitude: Double? = null,
    selectedLongitude: Double? = null,

    polygonPoints: List<MissionCoordinate> =
        emptyList(),

    participantMarkers:
    List<MissionParticipantMarker> =
        emptyList(),

    encounterMarkers:
    List<MissionEncounterMarker> =
        emptyList(),

    participantTracks:
    List<MissionTrackLine> =
        emptyList(),

    onMapClick: (
        latitude: Double,
        longitude: Double
    ) -> Unit = { _, _ -> },

    onInteractionChanged: (
        isInteracting: Boolean
    ) -> Unit = {},

    recenterKey: Any? = null,

    focusParticipantUserId: String? = null,

    onFocusParticipantHandled: () -> Unit = {},

    onParticipantClick: (
        participant: MissionParticipantMarker
    ) -> Unit = {},

    onEncounterClick: (
        encounterId: String
    ) -> Unit = {}
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

    /*
     * O AndroidView cria o listener do mapa apenas uma vez.
     * Sem rememberUpdatedState, o listener ficava preso à
     * lista inicial de participantes (normalmente vazia).
     */
    val currentParticipantMarkers by
    rememberUpdatedState(
        participantMarkers
    )

    val currentEncounterMarkers by
    rememberUpdatedState(
        encounterMarkers
    )

    val currentOnEncounterClick by
    rememberUpdatedState(
        onEncounterClick
    )

    val currentOnFocusParticipantHandled by
    rememberUpdatedState(
        onFocusParticipantHandled
    )

    val currentOnParticipantClick by
    rememberUpdatedState(
        onParticipantClick
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

    val density =
        LocalDensity.current

    var handledFocusParticipantUserId by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

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
                Style.Builder()
                    .fromUri(
                        styleUrl
                    )
            ) { style ->

                installMissionSourcesAndLayers(
                    style =
                        style
                )

                updateMissionSources(
                    style =
                        style,
                    selectedLatitude =
                        selectedLatitude,
                    selectedLongitude =
                        selectedLongitude,
                    polygonPoints =
                        polygonPoints,
                    participantMarkers =
                        participantMarkers,
                    encounterMarkers =
                        encounterMarkers,
                    participantTracks =
                        participantTracks
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
                        polygonPoints =
                            polygonPoints,
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

    LaunchedEffect(
        polygonPoints,
        selectedLatitude,
        selectedLongitude,
        participantMarkers,
        encounterMarkers,
        participantTracks,
        mapView
    ) {

        mapView.getMapAsync { map ->

            map.getStyle { style ->

                updateMissionSources(
                    style =
                        style,
                    selectedLatitude =
                        selectedLatitude,
                    selectedLongitude =
                        selectedLongitude,
                    polygonPoints =
                        polygonPoints,
                    participantMarkers =
                        participantMarkers,
                    encounterMarkers =
                        encounterMarkers,
                    participantTracks =
                        participantTracks
                )
            }
        }
    }

    LaunchedEffect(
        focusParticipantUserId,
        participantMarkers,
        mapView
    ) {

        val targetUserId =
            focusParticipantUserId
                ?.takeIf {
                    it.isNotBlank()
                }

        if (
            targetUserId == null
        ) {

            handledFocusParticipantUserId =
                null

            return@LaunchedEffect
        }

        if (
            handledFocusParticipantUserId ==
            targetUserId
        ) {
            return@LaunchedEffect
        }

        val participant =
            participantMarkers
                .firstOrNull {
                    it.userId ==
                            targetUserId
                }
                ?: return@LaunchedEffect

        mapView.getMapAsync { map ->

            map.animateCamera(
                CameraUpdateFactory
                    .newCameraPosition(
                        CameraPosition
                            .Builder()
                            .target(
                                LatLng(
                                    participant.latitude,
                                    participant.longitude
                                )
                            )
                            .zoom(
                                17.0
                            )
                            .build()
                    ),
                700
            )

            handledFocusParticipantUserId =
                targetUserId

            currentOnParticipantClick(
                participant
            )

            currentOnFocusParticipantHandled()
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

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(
                    400.dp
                )
    ) {

        AndroidView(
            modifier =
                Modifier
                    .matchParentSize(),

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

                            MotionEvent.ACTION_UP -> {

                                view.parent
                                    ?.requestDisallowInterceptTouchEvent(
                                        false
                                    )

                                currentOnInteractionChanged(
                                    false
                                )

                                view.performClick()
                            }

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

                        false
                    }

                    getMapAsync { map ->

                        map.addOnMapClickListener { point ->

                            val screenPoint =
                                map.projection
                                    .toScreenLocation(
                                        point
                                    )

                            /*
                             * Não dependemos mais do hit-test das layers
                             * do MapLibre. Procuramos diretamente o
                             * marcador mais próximo do toque em pixels.
                             *
                             * Isso funciona igualmente para:
                             * - marcador normal
                             * - apoio com coração
                             * - SOS
                             * - marcador do usuário atual com aro
                             */
                            val tapRadiusPx =
                                with(
                                    density
                                ) {
                                    36.dp.toPx()
                                }

                            val participantCandidate =
                                currentParticipantMarkers
                                    .map { marker ->

                                        val markerPoint =
                                            map.projection
                                                .toScreenLocation(
                                                    LatLng(
                                                        marker.latitude,
                                                        marker.longitude
                                                    )
                                                )

                                        val deltaX =
                                            markerPoint.x -
                                                    screenPoint.x

                                        val deltaY =
                                            markerPoint.y -
                                                    screenPoint.y

                                        val distanceSquared =
                                            (
                                                    deltaX *
                                                            deltaX
                                                    ) +
                                                    (
                                                            deltaY *
                                                                    deltaY
                                                            )

                                        marker to
                                                distanceSquared
                                    }
                                    .filter {
                                        it.second <=
                                                (
                                                        tapRadiusPx *
                                                                tapRadiusPx
                                                        )
                                    }
                                    .minByOrNull {
                                        it.second
                                    }

                            val encounterCandidate =
                                currentEncounterMarkers
                                    .map { marker ->

                                        val markerPoint =
                                            map.projection
                                                .toScreenLocation(
                                                    LatLng(
                                                        marker.latitude,
                                                        marker.longitude
                                                    )
                                                )

                                        val deltaX =
                                            markerPoint.x -
                                                    screenPoint.x

                                        val deltaY =
                                            markerPoint.y -
                                                    screenPoint.y

                                        val distanceSquared =
                                            (
                                                    deltaX *
                                                            deltaX
                                                    ) +
                                                    (
                                                            deltaY *
                                                                    deltaY
                                                            )

                                        marker to
                                                distanceSquared
                                    }
                                    .filter {
                                        it.second <=
                                                (
                                                        tapRadiusPx *
                                                                tapRadiusPx
                                                        )
                                    }
                                    .minByOrNull {
                                        it.second
                                    }

                            val shouldOpenEncounter =
                                encounterCandidate !=
                                        null &&
                                        (
                                                participantCandidate ==
                                                        null ||
                                                        encounterCandidate
                                                            .second <=
                                                        participantCandidate
                                                            .second
                                                )

                            when {

                                shouldOpenEncounter -> {

                                    currentOnEncounterClick(
                                        encounterCandidate
                                            ?.first
                                            ?.encounterId
                                            .orEmpty()
                                    )

                                    true
                                }

                                participantCandidate !=
                                        null -> {

                                    currentOnParticipantClick(
                                        participantCandidate
                                            .first
                                    )

                                    true
                                }

                                else -> {

                                    currentOnMapClick(
                                        point.latitude,
                                        point.longitude
                                    )

                                    true
                                }
                            }
                        }
                    }
                }
            },

            update = {
                Unit
            }
        )

    }

}

private fun installMissionSourcesAndLayers(
    style: Style
) {

    installParticipantMarkerImages(
        style =
            style
    )

    listOf(
        SOURCE_AREA_FILL,
        SOURCE_AREA_LINE,
        SOURCE_AREA_VERTICES,
        SOURCE_SELECTED_POINT,
        SOURCE_TRACKS,
        SOURCE_ENCOUNTERS,
        SOURCE_PARTICIPANTS
    )
        .forEach { sourceId ->

            addSourceIfMissing(
                style =
                    style,
                sourceId =
                    sourceId
            )
        }

    if (
        style.getLayer(
            LAYER_AREA_FILL
        ) == null
    ) {

        style.addLayer(
            FillLayer(
                LAYER_AREA_FILL,
                SOURCE_AREA_FILL
            )
                .withProperties(
                    PropertyFactory.fillColor(
                        COLOR_PRIMARY
                    ),
                    PropertyFactory.fillOpacity(
                        0.25f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_AREA_LINE
        ) == null
    ) {

        style.addLayer(
            LineLayer(
                LAYER_AREA_LINE,
                SOURCE_AREA_LINE
            )
                .withProperties(
                    PropertyFactory.lineColor(
                        COLOR_PRIMARY
                    ),
                    PropertyFactory.lineWidth(
                        5f
                    )
                )
        )
    }

    /*
     * Os trajetos ficam abaixo dos marcadores,
     * para a posição atual continuar evidente.
     */
    if (
        style.getLayer(
            LAYER_TRACKS
        ) == null
    ) {

        style.addLayer(
            LineLayer(
                LAYER_TRACKS,
                SOURCE_TRACKS
            )
                .withProperties(
                    PropertyFactory.lineColor(
                        Expression.toColor(
                            Expression.get(
                                PROPERTY_COLOR
                            )
                        )
                    ),
                    PropertyFactory.lineWidth(
                        5f
                    ),
                    PropertyFactory.lineOpacity(
                        0.82f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_AREA_VERTICES
        ) == null
    ) {

        style.addLayer(
            CircleLayer(
                LAYER_AREA_VERTICES,
                SOURCE_AREA_VERTICES
            )
                .withProperties(
                    PropertyFactory.circleRadius(
                        7f
                    ),
                    PropertyFactory.circleColor(
                        COLOR_PRIMARY
                    ),
                    PropertyFactory.circleStrokeColor(
                        Color.WHITE
                    ),
                    PropertyFactory.circleStrokeWidth(
                        3f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_SELECTED_POINT
        ) == null
    ) {

        style.addLayer(
            CircleLayer(
                LAYER_SELECTED_POINT,
                SOURCE_SELECTED_POINT
            )
                .withProperties(
                    PropertyFactory.circleRadius(
                        10f
                    ),
                    PropertyFactory.circleColor(
                        COLOR_PRIMARY
                    ),
                    PropertyFactory.circleStrokeColor(
                        Color.WHITE
                    ),
                    PropertyFactory.circleStrokeWidth(
                        4f
                    )
                )
        )
    }

    /*
     * Encontros registrados na missão.
     *
     * O círculo usa a cor do grupo que registrou o encontro
     * e a cruz branca diferencia esse ponto de um participante.
     */
    if (
        style.getLayer(
            LAYER_ENCOUNTERS_BASE
        ) == null
    ) {

        style.addLayer(
            CircleLayer(
                LAYER_ENCOUNTERS_BASE,
                SOURCE_ENCOUNTERS
            )
                .withProperties(
                    PropertyFactory.circleRadius(
                        12f
                    ),
                    PropertyFactory.circleColor(
                        Expression.toColor(
                            Expression.get(
                                PROPERTY_COLOR
                            )
                        )
                    ),
                    PropertyFactory.circleStrokeColor(
                        Color.WHITE
                    ),
                    PropertyFactory.circleStrokeWidth(
                        2.5f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_ENCOUNTERS_CROSS
        ) == null
    ) {

        style.addLayer(
            SymbolLayer(
                LAYER_ENCOUNTERS_CROSS,
                SOURCE_ENCOUNTERS
            )
                .withProperties(
                    PropertyFactory.iconImage(
                        IMAGE_ENCOUNTER_CROSS
                    ),
                    PropertyFactory.iconAllowOverlap(
                        true
                    ),
                    PropertyFactory.iconIgnorePlacement(
                        true
                    ),
                    PropertyFactory.iconSize(
                        0.52f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_CURRENT_USER
        ) == null
    ) {

        style.addLayer(
            CircleLayer(
                LAYER_CURRENT_USER,
                SOURCE_PARTICIPANTS
            )
                .withFilter(
                    Expression.eq(
                        Expression.get(
                            PROPERTY_IS_CURRENT_USER
                        ),
                        Expression.literal(
                            true
                        )
                    )
                )
                .withProperties(
                    PropertyFactory.circleRadius(
                        17f
                    ),
                    PropertyFactory.circleColor(
                        Color.WHITE
                    ),
                    PropertyFactory.circleOpacity(
                        0.94f
                    ),
                    PropertyFactory.circleStrokeColor(
                        COLOR_PRIMARY
                    ),
                    PropertyFactory.circleStrokeWidth(
                        2f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_PARTICIPANTS_ACTIVE
        ) == null
    ) {

        style.addLayer(
            CircleLayer(
                LAYER_PARTICIPANTS_ACTIVE,
                SOURCE_PARTICIPANTS
            )
                .withFilter(
                    Expression.eq(
                        Expression.get(
                            PROPERTY_STATUS
                        ),
                        Expression.literal(
                            MissionParticipantStatus
                                .ACTIVE
                                .name
                        )
                    )
                )
                .withProperties(
                    PropertyFactory.circleRadius(
                        11f
                    ),
                    PropertyFactory.circleColor(
                        Expression.toColor(
                            Expression.get(
                                PROPERTY_COLOR
                            )
                        )
                    ),
                    PropertyFactory.circleStrokeColor(
                        Color.WHITE
                    ),
                    PropertyFactory.circleStrokeWidth(
                        3f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_PARTICIPANTS_SUPPORT
        ) == null
    ) {

        style.addLayer(
            SymbolLayer(
                LAYER_PARTICIPANTS_SUPPORT,
                SOURCE_PARTICIPANTS
            )
                .withFilter(
                    Expression.all(
                        Expression.eq(
                            Expression.get(
                                PROPERTY_STATUS
                            ),
                            Expression.literal(
                                MissionParticipantStatus
                                    .ACTIVE
                                    .name
                            )
                        ),
                        Expression.eq(
                            Expression.get(
                                PROPERTY_IS_SUPPORT
                            ),
                            Expression.literal(
                                true
                            )
                        )
                    )
                )
                .withProperties(
                    PropertyFactory.iconImage(
                        IMAGE_SUPPORT
                    ),
                    PropertyFactory.iconAllowOverlap(
                        true
                    ),
                    PropertyFactory.iconIgnorePlacement(
                        true
                    ),
                    PropertyFactory.iconSize(
                        0.72f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_PARTICIPANTS_SOS_BASE
        ) == null
    ) {

        style.addLayer(
            CircleLayer(
                LAYER_PARTICIPANTS_SOS_BASE,
                SOURCE_PARTICIPANTS
            )
                .withFilter(
                    Expression.eq(
                        Expression.get(
                            PROPERTY_STATUS
                        ),
                        Expression.literal(
                            MissionParticipantStatus
                                .NEEDS_SUPPORT
                                .name
                        )
                    )
                )
                .withProperties(
                    PropertyFactory.circleRadius(
                        13f
                    ),
                    PropertyFactory.circleColor(
                        Expression.toColor(
                            Expression.get(
                                PROPERTY_COLOR
                            )
                        )
                    ),
                    PropertyFactory.circleStrokeColor(
                        Color.WHITE
                    ),
                    PropertyFactory.circleStrokeWidth(
                        4f
                    )
                )
        )
    }

    if (
        style.getLayer(
            LAYER_PARTICIPANTS_SOS
        ) == null
    ) {

        style.addLayer(
            SymbolLayer(
                LAYER_PARTICIPANTS_SOS,
                SOURCE_PARTICIPANTS
            )
                .withFilter(
                    Expression.eq(
                        Expression.get(
                            PROPERTY_STATUS
                        ),
                        Expression.literal(
                            MissionParticipantStatus
                                .NEEDS_SUPPORT
                                .name
                        )
                    )
                )
                .withProperties(
                    PropertyFactory.iconImage(
                        IMAGE_SOS
                    ),
                    PropertyFactory.iconAllowOverlap(
                        true
                    ),
                    PropertyFactory.iconIgnorePlacement(
                        true
                    ),
                    PropertyFactory.iconSize(
                        0.78f
                    )
                )
        )
    }

}

private fun installParticipantMarkerImages(
    style: Style
) {

    if (
        style.getImage(
            IMAGE_SUPPORT
        ) == null
    ) {
        style.addImage(
            IMAGE_SUPPORT,
            createSupportMarkerIcon()
        )
    }

    if (
        style.getImage(
            IMAGE_SOS
        ) == null
    ) {
        style.addImage(
            IMAGE_SOS,
            createSosMarkerIcon()
        )
    }

    if (
        style.getImage(
            IMAGE_ENCOUNTER_CROSS
        ) == null
    ) {
        style.addImage(
            IMAGE_ENCOUNTER_CROSS,
            createEncounterCrossIcon()
        )
    }
}

private fun createSupportMarkerIcon():
        Bitmap {

    /*
     * Apoio = coração branco.
     *
     * A cor continua vindo do grupo através da bolinha
     * do marcador. O coração identifica apenas a função
     * especial de apoio.
     */
    val size =
        48

    val bitmap =
        createBitmap(
            size,
            size
        )

    val canvas =
        Canvas(
            bitmap
        )

    val paint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {

            color =
                Color.WHITE

            style =
                Paint.Style.FILL
        }

    val heart =
        android.graphics.Path().apply {

            moveTo(
                24f,
                41f
            )

            cubicTo(
                20f,
                37f,
                7f,
                28f,
                7f,
                17f
            )

            cubicTo(
                7f,
                10f,
                12f,
                6f,
                18f,
                6f
            )

            cubicTo(
                21f,
                6f,
                23f,
                8f,
                24f,
                10f
            )

            cubicTo(
                25f,
                8f,
                27f,
                6f,
                30f,
                6f
            )

            cubicTo(
                36f,
                6f,
                41f,
                10f,
                41f,
                17f
            )

            cubicTo(
                41f,
                28f,
                28f,
                37f,
                24f,
                41f
            )

            close()
        }

    canvas.drawPath(
        heart,
        paint
    )

    return bitmap
}

private fun createSosMarkerIcon():
        Bitmap {

    val width =
        72

    val height =
        40

    val bitmap =
        createBitmap(
            width,
            height
        )

    val canvas =
        Canvas(
            bitmap
        )

    val paint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            color =
                Color.WHITE
            textAlign =
                Paint.Align.CENTER
            textSize =
                22f
            isFakeBoldText =
                true
        }

    canvas.drawText(
        "SOS",
        width /
                2f,
        height /
                2f -
                (
                        paint.ascent() +
                                paint.descent()
                        ) /
                2f,
        paint
    )

    return bitmap
}


private fun createEncounterCrossIcon():
        Bitmap {

    val size =
        48

    val bitmap =
        createBitmap(
            size,
            size
        )

    val canvas =
        Canvas(
            bitmap
        )

    val paint =
        Paint(
            Paint.ANTI_ALIAS_FLAG
        ).apply {
            color =
                Color.WHITE

            style =
                Paint.Style.STROKE

            strokeWidth =
                7f

            strokeCap =
                Paint.Cap.ROUND
        }

    val centerX =
        size / 2f

    canvas.drawLine(
        centerX,
        8f,
        centerX,
        40f,
        paint
    )

    canvas.drawLine(
        15f,
        20f,
        33f,
        20f,
        paint
    )

    return bitmap
}

private fun addSourceIfMissing(
    style: Style,
    sourceId: String
) {

    if (
        style.getSource(
            sourceId
        ) != null
    ) {
        return
    }

    style.addSource(
        GeoJsonSource(
            sourceId,
            emptyFeatureCollectionJson()
        )
    )
}

private fun updateMissionSources(
    style: Style,
    selectedLatitude: Double?,
    selectedLongitude: Double?,
    polygonPoints: List<MissionCoordinate>,
    participantMarkers: List<MissionParticipantMarker>,
    encounterMarkers: List<MissionEncounterMarker>,
    participantTracks: List<MissionTrackLine>
) {

    source(
        style,
        SOURCE_SELECTED_POINT
    )
        ?.setGeoJson(
            selectedPointGeoJson(
                selectedLatitude,
                selectedLongitude
            )
        )

    source(
        style,
        SOURCE_AREA_VERTICES
    )
        ?.setGeoJson(
            verticesGeoJson(
                polygonPoints
            )
        )

    source(
        style,
        SOURCE_AREA_LINE
    )
        ?.setGeoJson(
            areaLineGeoJson(
                polygonPoints
            )
        )

    source(
        style,
        SOURCE_AREA_FILL
    )
        ?.setGeoJson(
            areaPolygonGeoJson(
                polygonPoints
            )
        )

    source(
        style,
        SOURCE_TRACKS
    )
        ?.setGeoJson(
            tracksGeoJson(
                participantTracks
            )
        )

    source(
        style,
        SOURCE_ENCOUNTERS
    )
        ?.setGeoJson(
            encountersGeoJson(
                encounterMarkers
            )
        )

    source(
        style,
        SOURCE_PARTICIPANTS
    )
        ?.setGeoJson(
            participantsGeoJson(
                participantMarkers
            )
        )
}

private fun source(
    style: Style,
    id: String
): GeoJsonSource? {

    return style.getSourceAs(
        id
    )
}

private fun centerArea(
    map: MapLibreMap,
    polygonPoints: List<MissionCoordinate>,
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

    val update =
        CameraUpdateFactory
            .newLatLngBounds(
                boundsBuilder.build(),
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

private fun tracksGeoJson(
    tracks: List<MissionTrackLine>
): String {

    val features =
        tracks
            .mapNotNull { track ->

                if (
                    track.points.size <
                    2
                ) {
                    return@mapNotNull null
                }

                val coordinates =
                    JSONArray()

                track.points
                    .forEach { point ->

                        coordinates.put(
                            coordinateJson(
                                point.latitude,
                                point.longitude
                            )
                        )
                    }

                val geometry =
                    JSONObject()
                        .put(
                            "type",
                            "LineString"
                        )
                        .put(
                            "coordinates",
                            coordinates
                        )

                val color =
                    track.colorHex
                        ?.takeIf {
                            it.isValidHexColor()
                        }
                        ?: DEFAULT_PARTICIPANT_COLOR

                val properties =
                    JSONObject()
                        .put(
                            PROPERTY_USER_ID,
                            track.userId
                        )
                        .put(
                            PROPERTY_GROUP_ID,
                            track.groupId
                                ?: JSONObject.NULL
                        )
                        .put(
                            PROPERTY_COLOR,
                            color
                        )

                JSONObject()
                    .put(
                        "type",
                        "Feature"
                    )
                    .put(
                        "properties",
                        properties
                    )
                    .put(
                        "geometry",
                        geometry
                    )
            }

    return featureCollectionJson(
        *features.toTypedArray()
    )
}

private fun encountersGeoJson(
    markers: List<MissionEncounterMarker>
): String {

    val features =
        markers
            .map { marker ->

                val color =
                    marker.colorHex
                        ?.takeIf {
                            it.isValidHexColor()
                        }
                        ?: DEFAULT_PARTICIPANT_COLOR

                val properties =
                    JSONObject()
                        .put(
                            PROPERTY_ENCOUNTER_ID,
                            marker.encounterId
                        )
                        .put(
                            PROPERTY_USER_ID,
                            marker.registeredByUserId
                        )
                        .put(
                            PROPERTY_GROUP_ID,
                            marker.groupId
                                ?: JSONObject.NULL
                        )
                        .put(
                            PROPERTY_COLOR,
                            color
                        )
                        .put(
                            PROPERTY_PERSON_NAME,
                            marker.personName
                                ?: JSONObject.NULL
                        )

                pointFeature(
                    marker.latitude,
                    marker.longitude,
                    properties
                )
            }

    return featureCollectionJson(
        *features.toTypedArray()
    )
}

private fun participantsGeoJson(
    markers: List<MissionParticipantMarker>
): String {

    val features =
        markers
            .map { marker ->

                val color =
                    marker.colorHex
                        ?.takeIf {
                            it.isValidHexColor()
                        }
                        ?: DEFAULT_PARTICIPANT_COLOR

                val properties =
                    JSONObject()
                        .put(
                            PROPERTY_USER_ID,
                            marker.userId
                        )
                        .put(
                            PROPERTY_GROUP_ID,
                            marker.groupId
                                ?: JSONObject.NULL
                        )
                        .put(
                            PROPERTY_COLOR,
                            color
                        )
                        .put(
                            PROPERTY_STATUS,
                            marker.status.name
                        )
                        .put(
                            PROPERTY_IS_SUPPORT,
                            marker.isSupport
                        )
                        .put(
                            PROPERTY_IS_CURRENT_USER,
                            marker.isCurrentUser
                        )
                        .put(
                            PROPERTY_DISPLAY_NAME,
                            marker.displayName
                        )

                pointFeature(
                    marker.latitude,
                    marker.longitude,
                    properties
                )
            }

    return featureCollectionJson(
        *features.toTypedArray()
    )
}

private fun selectedPointGeoJson(
    latitude: Double?,
    longitude: Double?
): String {

    if (
        latitude == null ||
        longitude == null
    ) {
        return emptyFeatureCollectionJson()
    }

    return featureCollectionJson(
        pointFeature(
            latitude,
            longitude
        )
    )
}

private fun verticesGeoJson(
    points: List<MissionCoordinate>
): String {

    return featureCollectionJson(
        *points
            .map { point ->
                pointFeature(
                    point.latitude,
                    point.longitude
                )
            }
            .toTypedArray()
    )
}

private fun areaLineGeoJson(
    points: List<MissionCoordinate>
): String {

    if (
        points.size <
        2
    ) {
        return emptyFeatureCollectionJson()
    }

    val coordinates =
        JSONArray()

    points
        .forEach { point ->
            coordinates.put(
                coordinateJson(
                    point.latitude,
                    point.longitude
                )
            )
        }

    if (
        points.size >=
        3
    ) {

        val first =
            points.first()

        coordinates.put(
            coordinateJson(
                first.latitude,
                first.longitude
            )
        )
    }

    return featureCollectionJson(
        JSONObject()
            .put(
                "type",
                "Feature"
            )
            .put(
                "properties",
                JSONObject()
            )
            .put(
                "geometry",
                JSONObject()
                    .put(
                        "type",
                        "LineString"
                    )
                    .put(
                        "coordinates",
                        coordinates
                    )
            )
    )
}

private fun areaPolygonGeoJson(
    points: List<MissionCoordinate>
): String {

    if (
        points.size <
        3
    ) {
        return emptyFeatureCollectionJson()
    }

    val ring =
        JSONArray()

    points
        .forEach { point ->
            ring.put(
                coordinateJson(
                    point.latitude,
                    point.longitude
                )
            )
        }

    val first =
        points.first()

    ring.put(
        coordinateJson(
            first.latitude,
            first.longitude
        )
    )

    return featureCollectionJson(
        JSONObject()
            .put(
                "type",
                "Feature"
            )
            .put(
                "properties",
                JSONObject()
            )
            .put(
                "geometry",
                JSONObject()
                    .put(
                        "type",
                        "Polygon"
                    )
                    .put(
                        "coordinates",
                        JSONArray()
                            .put(
                                ring
                            )
                    )
            )
    )
}

private fun pointFeature(
    latitude: Double,
    longitude: Double,
    properties: JSONObject =
        JSONObject()
): JSONObject {

    return JSONObject()
        .put(
            "type",
            "Feature"
        )
        .put(
            "properties",
            properties
        )
        .put(
            "geometry",
            JSONObject()
                .put(
                    "type",
                    "Point"
                )
                .put(
                    "coordinates",
                    coordinateJson(
                        latitude,
                        longitude
                    )
                )
        )
}

private fun coordinateJson(
    latitude: Double,
    longitude: Double
): JSONArray {

    return JSONArray()
        .put(
            longitude
        )
        .put(
            latitude
        )
}

private fun featureCollectionJson(
    vararg features: JSONObject
): String {

    val array =
        JSONArray()

    features
        .forEach(
            array::put
        )

    return JSONObject()
        .put(
            "type",
            "FeatureCollection"
        )
        .put(
            "features",
            array
        )
        .toString()
}

private fun emptyFeatureCollectionJson():
        String {

    return featureCollectionJson()
}

private fun String.isValidHexColor():
        Boolean {

    return matches(
        Regex(
            "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"
        )
    )
}

private const val SOURCE_AREA_FILL =
    "mission-area-fill-source"

private const val SOURCE_AREA_LINE =
    "mission-area-line-source"

private const val SOURCE_AREA_VERTICES =
    "mission-area-vertices-source"

private const val SOURCE_SELECTED_POINT =
    "mission-selected-point-source"

private const val SOURCE_TRACKS =
    "mission-tracks-source"

private const val SOURCE_PARTICIPANTS =
    "mission-participants-source"

private const val SOURCE_ENCOUNTERS =
    "mission-encounters-source"

private const val LAYER_AREA_FILL =
    "mission-area-fill-layer"

private const val LAYER_AREA_LINE =
    "mission-area-line-layer"

private const val LAYER_TRACKS =
    "mission-tracks-layer"

private const val LAYER_AREA_VERTICES =
    "mission-area-vertices-layer"

private const val LAYER_SELECTED_POINT =
    "mission-selected-point-layer"

private const val LAYER_ENCOUNTERS_BASE =
    "mission-encounters-base-layer"

private const val LAYER_ENCOUNTERS_CROSS =
    "mission-encounters-cross-layer"

private const val LAYER_CURRENT_USER =
    "mission-current-user-layer"

private const val LAYER_PARTICIPANTS_ACTIVE =
    "mission-participants-active-layer"

private const val LAYER_PARTICIPANTS_SUPPORT =
    "mission-participants-support-layer"

private const val LAYER_PARTICIPANTS_SOS_BASE =
    "mission-participants-sos-base-layer"

private const val LAYER_PARTICIPANTS_SOS =
    "mission-participants-sos-layer"

private const val IMAGE_SUPPORT =
    "mission-participant-support-icon"

private const val IMAGE_SOS =
    "mission-participant-sos-icon"

private const val IMAGE_ENCOUNTER_CROSS =
    "mission-encounter-cross-icon"

private const val PROPERTY_ENCOUNTER_ID =
    "encounterId"

private const val PROPERTY_PERSON_NAME =
    "personName"

private const val PROPERTY_USER_ID =
    "userId"

private const val PROPERTY_GROUP_ID =
    "groupId"

private const val PROPERTY_COLOR =
    "color"

private const val PROPERTY_STATUS =
    "status"

private const val PROPERTY_IS_SUPPORT =
    "isSupport"

private const val PROPERTY_IS_CURRENT_USER =
    "isCurrentUser"

private const val PROPERTY_DISPLAY_NAME =
    "displayName"

private const val DEFAULT_PARTICIPANT_COLOR =
    "#8B8D98"

private const val COLOR_PRIMARY =
    0xFF2EC4B6.toInt()
