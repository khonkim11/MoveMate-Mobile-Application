package com.example.movemate

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private const val LOCATION_INTERVAL_MILLIS =
    1_500L

private const val LOCATION_MIN_INTERVAL_MILLIS =
    750L

private const val MAX_ACCEPTED_ACCURACY_METRES =
    35f

private const val MIN_ACCEPTED_STEP_METRES =
    1.2

private const val CAMERA_FOLLOW_INTERVAL_MILLIS =
    320L

private const val CAMERA_ANIMATION_MILLIS =
    300

private const val MARKER_ANIMATION_MILLIS =
    850

private const val EARTH_RADIUS_METRES =
    6_371_000.0

/*
 * Emulator start near the user's Mandalay test area.
 */
private val DEFAULT_EMULATOR_START =
    LatLng(
        21.982250,
        96.097938
    )

/**
 * Shared road map for every activity with tracksRoute = true.
 *
 * Running, Walking and Cycling all use this same implementation.
 *
 * Emulator:
 * - route shape comes from OneWayRouteApi;
 * - movement is calculated from elapsed workout time;
 * - the point reaches the end exactly at targetSeconds.
 *
 * Physical phone:
 * - GPS determines actual travelled distance;
 * - the visible point is projected along the planned road route;
 * - GPS noise cannot draw the marker across buildings or water.
 */
@SuppressLint(
    "MissingPermission"
)
@Composable
fun WorkoutRouteMap(
    userId: Int,
    title: String,
    activityName: String,
    expectedSpeedKmh: Double,
    isRunning: Boolean,
    isFinished: Boolean,
    elapsedSeconds: Int,
    targetSeconds: Int,
    elapsedMilliseconds: Long =
        elapsedSeconds
            .toLong() *
                1_000L,
    currentDistanceKm: Double,
    targetDistanceKm: Double,
    initialRoutePoints: List<LatLng> =
        emptyList(),
    accentColor: Color,
    onActualDistanceChanged:
        (
        Double
    ) -> Unit = {},
    onRouteChanged:
        (
        List<LatLng>
    ) -> Unit = {},
    modifier: Modifier =
        Modifier
) {
    val context =
        LocalContext.current

    val runningOnEmulator =
        remember {
            moveMateIsEmulator()
        }

    val latestDistanceCallback =
        rememberUpdatedState(
            onActualDistanceChanged
        )

    val latestRouteCallback =
        rememberUpdatedState(
            onRouteChanged
        )

    val latestIsRunning =
        rememberUpdatedState(
            isRunning
        )

    var locationGranted by
    remember {
        mutableStateOf(
            moveMateHasLocationPermission(
                context
            )
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) {
            locationGranted =
                moveMateHasLocationPermission(
                    context
                )
        }

    LaunchedEffect(
        runningOnEmulator,
        locationGranted
    ) {
        if (
            !runningOnEmulator &&
            !locationGranted
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission
                        .ACCESS_FINE_LOCATION,
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val plannedRoad =
        remember(
            userId,
            title,
            activityName,
            targetDistanceKm
        ) {
            mutableStateListOf<LatLng>()
        }

    var originPoint by
    remember(
        userId,
        title
    ) {
        mutableStateOf<LatLng?>(
            initialRoutePoints
                .firstOrNull()
                ?: if (
                    runningOnEmulator
                ) {
                    DEFAULT_EMULATOR_START
                } else {
                    null
                }
        )
    }

    var phoneDistanceKm by
    remember(
        userId,
        title
    ) {
        mutableStateOf(
            currentDistanceKm
                .coerceAtLeast(
                    0.0
                )
        )
    }

    var lastAcceptedLocation by
    remember(
        userId,
        title
    ) {
        mutableStateOf<Location?>(
            null
        )
    }

    var roadLoading by
    remember(
        userId,
        title,
        activityName,
        targetDistanceKm
    ) {
        mutableStateOf(
            false
        )
    }

    var roadError by
    remember(
        userId,
        title,
        activityName,
        targetDistanceKm
    ) {
        mutableStateOf(
            ""
        )
    }

    var mapType by
    remember {
        mutableStateOf(
            MapType.NORMAL
        )
    }

    var followMovingPoint by
    remember {
        mutableStateOf(
            true
        )
    }

    val exactTargetMetres =
        remember(
            targetDistanceKm,
            expectedSpeedKmh,
            targetSeconds
        ) {
            val durationDistance =
                expectedSpeedKmh
                    .coerceAtLeast(
                        0.1
                    ) *
                        targetSeconds
                            .coerceAtLeast(
                                1
                            )
                            .toDouble() /
                        3_600.0

            (
                    targetDistanceKm
                        .takeIf {
                            it >
                                    0.0
                        }
                        ?: durationDistance
                    )
                .coerceAtLeast(
                    0.05
                ) *
                    1_000.0
        }

    /*
     * Keep local state synchronized when an active workout is reopened.
     */
    LaunchedEffect(
        currentDistanceKm,
        runningOnEmulator
    ) {
        if (
            !runningOnEmulator &&
            currentDistanceKm >
            phoneDistanceKm
        ) {
            phoneDistanceKm =
                currentDistanceKm
        }
    }

    val fusedLocationClient =
        remember(
            context
        ) {
            LocationServices
                .getFusedLocationProviderClient(
                    context
                )
        }

    val maximumReasonableSpeedMetresPerSecond =
        remember(
            activityName,
            expectedSpeedKmh
        ) {
            val selectedSpeed =
                expectedSpeedKmh
                    .coerceAtLeast(
                        1.0
                    ) /
                        3.6

            val activityMaximum =
                when {
                    activityName.equals(
                        "Walking",
                        ignoreCase =
                            true
                    ) ->
                        3.2

                    activityName.equals(
                        "Cycling",
                        ignoreCase =
                            true
                    ) ->
                        16.0

                    else ->
                        7.0
                }

            min(
                activityMaximum,
                max(
                    selectedSpeed *
                            2.5,
                    selectedSpeed +
                            2.0
                )
            )
        }

    val locationRequest =
        remember(
            activityName,
            expectedSpeedKmh
        ) {
            LocationRequest
                .Builder(
                    Priority
                        .PRIORITY_HIGH_ACCURACY,
                    LOCATION_INTERVAL_MILLIS
                )
                .setMinUpdateIntervalMillis(
                    LOCATION_MIN_INTERVAL_MILLIS
                )
                .setMinUpdateDistanceMeters(
                    0f
                )
                .build()
        }

    val locationCallback =
        remember(
            userId,
            title,
            maximumReasonableSpeedMetresPerSecond
        ) {
            object :
                LocationCallback() {

                override fun onLocationResult(
                    result:
                    LocationResult
                ) {
                    result.locations
                        .sortedBy {
                            it.elapsedRealtimeNanos
                        }
                        .forEach {
                                location ->

                            if (
                                location.accuracy >
                                MAX_ACCEPTED_ACCURACY_METRES
                            ) {
                                return@forEach
                            }

                            val previous =
                                lastAcceptedLocation

                            if (
                                previous ==
                                null
                            ) {
                                lastAcceptedLocation =
                                    Location(
                                        location
                                    )

                                originPoint =
                                    originPoint
                                        ?: LatLng(
                                            location.latitude,
                                            location.longitude
                                        )

                                return@forEach
                            }

                            if (
                                !latestIsRunning.value
                            ) {
                                lastAcceptedLocation =
                                    Location(
                                        location
                                    )

                                return@forEach
                            }

                            val stepMetres =
                                previous
                                    .distanceTo(
                                        location
                                    )
                                    .toDouble()

                            val elapsedNanos =
                                (
                                        location.elapsedRealtimeNanos -
                                                previous.elapsedRealtimeNanos
                                        )
                                    .coerceAtLeast(
                                        1L
                                    )

                            val elapsedSecondsBetweenPoints =
                                elapsedNanos
                                    .toDouble() /
                                        1_000_000_000.0

                            val measuredSpeed =
                                stepMetres /
                                        elapsedSecondsBetweenPoints

                            if (
                                stepMetres <
                                MIN_ACCEPTED_STEP_METRES ||
                                measuredSpeed >
                                maximumReasonableSpeedMetresPerSecond
                            ) {
                                return@forEach
                            }

                            lastAcceptedLocation =
                                Location(
                                    location
                                )

                            phoneDistanceKm =
                                (
                                        phoneDistanceKm +
                                                stepMetres /
                                                1_000.0
                                        )
                                    .coerceAtMost(
                                        exactTargetMetres /
                                                1_000.0
                                    )

                            latestDistanceCallback
                                .value(
                                    phoneDistanceKm
                                )
                        }
                }
            }
        }

    /*
     * Obtain the first physical-phone location before requesting the road.
     */
    LaunchedEffect(
        runningOnEmulator,
        locationGranted,
        userId,
        title
    ) {
        if (
            runningOnEmulator ||
            !locationGranted
        ) {
            return@LaunchedEffect
        }

        if (
            ActivityCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ) !=
            PackageManager
                .PERMISSION_GRANTED &&
            ActivityCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ) !=
            PackageManager
                .PERMISSION_GRANTED
        ) {
            return@LaunchedEffect
        }

        fusedLocationClient
            .lastLocation
            .addOnSuccessListener {
                    location ->

                if (
                    location !=
                    null &&
                    location.accuracy <=
                    MAX_ACCEPTED_ACCURACY_METRES
                ) {
                    originPoint =
                        LatLng(
                            location.latitude,
                            location.longitude
                        )

                    lastAcceptedLocation =
                        Location(
                            location
                        )
                }
            }
    }

    DisposableEffect(
        runningOnEmulator,
        locationGranted,
        isFinished,
        locationCallback
    ) {
        if (
            !runningOnEmulator &&
            locationGranted &&
            !isFinished &&
            moveMateHasLocationPermission(
                context
            )
        ) {
            fusedLocationClient
                .requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
        }

        onDispose {
            fusedLocationClient
                .removeLocationUpdates(
                    locationCallback
                )
        }
    }

    /*
     * Request one road-only route and trim it to the exact selected duration.
     *
     * Movement is local after this point. The network is not called on every
     * timer update.
     */
    LaunchedEffect(
        originPoint,
        exactTargetMetres,
        activityName
    ) {
        val origin =
            originPoint
                ?: return@LaunchedEffect

        roadLoading =
            true

        roadError =
            ""

        try {
            val loadedRoute =
                OneWayRouteApi
                    .load(
                        origin =
                            origin,
                        targetDistanceMetres =
                            exactTargetMetres,
                        activityName =
                            activityName
                    )

            val exactRoad =
                moveMateTrimRoadToDistance(
                    road =
                        loadedRoute.points,
                    targetDistanceMetres =
                        exactTargetMetres
                )

            if (
                exactRoad.size <
                2
            ) {
                throw IllegalStateException(
                    "The road service returned an unusable route."
                )
            }

            plannedRoad.clear()

            plannedRoad.addAll(
                exactRoad
            )

            originPoint =
                exactRoad.first()

            latestRouteCallback
                .value(
                    listOf(
                        exactRoad.first()
                    )
                )
        } catch (
            exception: Exception
        ) {
            /*
             * The PHP route service can temporarily return HTTP 502.
             *
             * Do not leave plannedRoad empty because the timer, distance and
             * calories will continue while the map marker remains at Start.
             *
             * Build a local grid-style route so the moving point can continue
             * smoothly. The API route is still preferred whenever available.
             */
            val fallbackRoad =
                moveMateBuildFallbackRoadRoute(
                    origin =
                        origin,
                    targetDistanceMetres =
                        exactTargetMetres,
                    activityName =
                        activityName
                )

            plannedRoad.clear()

            plannedRoad.addAll(
                fallbackRoad
            )

            originPoint =
                fallbackRoad.firstOrNull()
                    ?: origin

            /*
             * Clear the blocking error because a usable local route now
             * exists. The marker, travelled line and camera can all move.
             */
            roadError =
                ""

            latestRouteCallback
                .value(
                    listOf(
                        originPoint
                            ?: origin
                    )
                )
        } finally {
            roadLoading =
                false
        }
    }

    /*
     * Emulator distance is controlled by active workout time.
     *
     * A 10-minute route reaches the endpoint at exactly 10:00.
     */
    val exactElapsedDistanceMetres =
        if (
            runningOnEmulator
        ) {
            val targetMilliseconds =
                targetSeconds
                    .coerceAtLeast(
                        1
                    )
                    .toLong() *
                        1_000L

            val progress =
                (
                        elapsedMilliseconds
                            .coerceIn(
                                0L,
                                targetMilliseconds
                            )
                            .toDouble() /
                                targetMilliseconds
                                    .toDouble()
                        )
                    .coerceIn(
                        0.0,
                        1.0
                    )

            exactTargetMetres *
                    progress
        } else {
            phoneDistanceKm *
                    1_000.0
        }

    /*
     * Smoothly animate between timer/GPS updates.
     *
     * This removes the old one-second marker jumps.
     */
    val smoothDistanceMetres by
    animateFloatAsState(
        targetValue =
            exactElapsedDistanceMetres
                .coerceIn(
                    0.0,
                    exactTargetMetres
                )
                .toFloat(),
        animationSpec =
            tween(
                durationMillis =
                    if (
                        isRunning
                    ) {
                        MARKER_ANIMATION_MILLIS
                    } else {
                        120
                    },
                easing =
                    LinearEasing
            ),
        label =
            "smooth_road_distance"
    )

    /*
     * Keep the manager distance synchronized with the emulator timer.
     */
    LaunchedEffect(
        runningOnEmulator,
        elapsedMilliseconds,
        targetSeconds,
        exactTargetMetres
    ) {
        if (
            runningOnEmulator
        ) {
            latestDistanceCallback
                .value(
                    exactElapsedDistanceMetres /
                            1_000.0
                )
        }
    }

    val visibleDistanceMetres =
        smoothDistanceMetres
            .toDouble()
            .coerceIn(
                0.0,
                exactTargetMetres
            )

    val movingPoint =
        if (
            plannedRoad.size >=
            2
        ) {
            moveMatePointAtRoadDistance(
                road =
                    plannedRoad,
                distanceMetres =
                    visibleDistanceMetres
            )
        } else {
            originPoint
        }

    val travelledRoad =
        if (
            plannedRoad.size >=
            2
        ) {
            moveMateRoadPrefixAtDistance(
                road =
                    plannedRoad,
                distanceMetres =
                    visibleDistanceMetres
            )
        } else {
            movingPoint
                ?.let {
                    listOf(
                        it
                    )
                }
                .orEmpty()
        }

    /*
     * Save a compact route snapshot once per displayed timer second.
     */
    LaunchedEffect(
        elapsedSeconds,
        plannedRoad.size,
        runningOnEmulator,
        phoneDistanceKm
    ) {
        if (
            plannedRoad.size >=
            2
        ) {
            latestRouteCallback
                .value(
                    moveMateRoadPrefixAtDistance(
                        road =
                            plannedRoad,
                        distanceMetres =
                            exactElapsedDistanceMetres
                                .coerceIn(
                                    0.0,
                                    exactTargetMetres
                                )
                    )
                )
        }
    }

    val initialCameraPoint =
        movingPoint
            ?: plannedRoad
                .firstOrNull()
            ?: originPoint
            ?: DEFAULT_EMULATOR_START

    val cameraPositionState =
        rememberCameraPositionState {
            position =
                CameraPosition
                    .fromLatLngZoom(
                        initialCameraPoint,
                        16.8f
                    )
        }

    val startMarkerState =
        remember {
            MarkerState(
                position =
                    initialCameraPoint
            )
        }

    val finishMarkerState =
        remember {
            MarkerState(
                position =
                    initialCameraPoint
            )
        }

    val movingMarkerState =
        remember {
            MarkerState(
                position =
                    initialCameraPoint
            )
        }

    LaunchedEffect(
        plannedRoad.firstOrNull()
    ) {
        plannedRoad
            .firstOrNull()
            ?.let {
                startMarkerState.position =
                    it
            }
    }

    LaunchedEffect(
        plannedRoad.lastOrNull()
    ) {
        plannedRoad
            .lastOrNull()
            ?.let {
                finishMarkerState.position =
                    it
            }
    }

    LaunchedEffect(
        movingPoint
    ) {
        movingPoint
            ?.let {
                movingMarkerState.position =
                    it
            }
    }

    /*
     * Fit the road when it first loads.
     */
    LaunchedEffect(
        plannedRoad.size
    ) {
        if (
            plannedRoad.size <
            2
        ) {
            return@LaunchedEffect
        }

        val boundsBuilder =
            LatLngBounds
                .Builder()

        plannedRoad.forEach {
            boundsBuilder.include(
                it
            )
        }

        runCatching {
            cameraPositionState.animate(
                update =
                    CameraUpdateFactory
                        .newLatLngBounds(
                            boundsBuilder.build(),
                            90
                        ),
                durationMs =
                    500
            )
        }

        delay(
            650L
        )

        followMovingPoint =
            true
    }

    val latestMovingPoint =
        rememberUpdatedState(
            movingPoint
        )

    /*
     * Camera follows the point at a lower frequency than marker animation.
     * The point remains fluid while the map camera avoids shaking.
     */
    LaunchedEffect(
        followMovingPoint,
        isRunning,
        isFinished
    ) {
        while (
            isActive &&
            followMovingPoint &&
            !isFinished
        ) {
            delay(
                CAMERA_FOLLOW_INTERVAL_MILLIS
            )

            val point =
                latestMovingPoint.value
                    ?: continue

            cameraPositionState.animate(
                update =
                    CameraUpdateFactory
                        .newLatLngZoom(
                            point,
                            17.2f
                        ),
                durationMs =
                    CAMERA_ANIMATION_MILLIS
            )
        }
    }

    Card(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                26.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF071426)
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    5.dp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    390.dp
                )
        ) {
            GoogleMap(
                modifier =
                    Modifier.matchParentSize(),
                cameraPositionState =
                    cameraPositionState,
                properties =
                    MapProperties(
                        isMyLocationEnabled =
                            !runningOnEmulator &&
                                    locationGranted,
                        mapType =
                            mapType
                    ),
                uiSettings =
                    MapUiSettings(
                        myLocationButtonEnabled =
                            false,
                        compassEnabled =
                            true,
                        zoomControlsEnabled =
                            false,
                        mapToolbarEnabled =
                            false,
                        scrollGesturesEnabled =
                            true,
                        zoomGesturesEnabled =
                            true,
                        rotationGesturesEnabled =
                            true,
                        tiltGesturesEnabled =
                            false
                    ),
                onMapClick = {
                    followMovingPoint =
                        false
                }
            ) {
                /*
                 * Planned road shadow.
                 */
                if (
                    plannedRoad.size >=
                    2
                ) {
                    Polyline(
                        points =
                            plannedRoad,
                        color =
                            Color.White.copy(
                                alpha =
                                    0.88f
                            ),
                        width =
                            19f,
                        geodesic =
                            false,
                        zIndex =
                            1f
                    )

                    /*
                     * Planned road guide.
                     */
                    Polyline(
                        points =
                            plannedRoad,
                        color =
                            Color(0xFF60A5FA),
                        width =
                            11f,
                        geodesic =
                            false,
                        zIndex =
                            2f
                    )
                }

                /*
                 * Completed road shadow.
                 */
                if (
                    travelledRoad.size >=
                    2
                ) {
                    Polyline(
                        points =
                            travelledRoad,
                        color =
                            Color(0xFF083344),
                        width =
                            21f,
                        geodesic =
                            false,
                        zIndex =
                            3f
                    )

                    /*
                     * Completed road line.
                     */
                    Polyline(
                        points =
                            travelledRoad,
                        color =
                            accentColor,
                        width =
                            13f,
                        geodesic =
                            false,
                        zIndex =
                            4f
                    )
                }

                if (
                    plannedRoad.size >=
                    2
                ) {
                    Marker(
                        state =
                            startMarkerState,
                        title =
                            "Workout start",
                        icon =
                            BitmapDescriptorFactory
                                .defaultMarker(
                                    BitmapDescriptorFactory
                                        .HUE_GREEN
                                ),
                        zIndex =
                            5f
                    )

                    Marker(
                        state =
                            finishMarkerState,
                        title =
                            "Workout finish",
                        snippet =
                            String.format(
                                Locale.US,
                                "%.2f km target",
                                exactTargetMetres /
                                        1_000.0
                            ),
                        icon =
                            BitmapDescriptorFactory
                                .defaultMarker(
                                    BitmapDescriptorFactory
                                        .HUE_ORANGE
                                ),
                        zIndex =
                            5f
                    )
                }

                movingPoint
                    ?.let {
                        Marker(
                            state =
                                movingMarkerState,
                            title =
                                "$activityName position",
                            icon =
                                BitmapDescriptorFactory
                                    .defaultMarker(
                                        BitmapDescriptorFactory
                                            .HUE_AZURE
                                    ),
                            zIndex =
                                7f
                        )
                    }
            }

            /*
             * Small map status chip.
             */
            Surface(
                modifier = Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .padding(
                        12.dp
                    ),
                shape =
                    RoundedCornerShape(
                        999.dp
                    ),
                color =
                    Color(0xE60F172A)
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal =
                                12.dp,
                            vertical =
                                8.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            7.dp
                        )
                ) {
                    if (
                        roadLoading
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    15.dp
                                ),
                            color =
                                Color.White,
                            strokeWidth =
                                2.dp
                        )
                    }

                    Text(
                        text =
                            when {
                                roadLoading ->
                                    "Preparing road"

                                roadError
                                    .isNotBlank() ->
                                    "Road unavailable"

                                isFinished ->
                                    "Finished"

                                isRunning ->
                                    "LIVE • $activityName"

                                elapsedSeconds >
                                        0 ->
                                    "Paused"

                                else ->
                                    "Ready"
                            },
                        color =
                            Color.White,
                        fontSize =
                            11.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            /*
             * Map controls.
             */
            Column(
                modifier = Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(
                        12.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        9.dp
                    ),
                horizontalAlignment =
                    Alignment.End
            ) {
                MoveMateMapControl(
                    text =
                        "◎",
                    onClick = {
                        followMovingPoint =
                            true
                    }
                )

                MoveMateMapControl(
                    text =
                        if (
                            mapType ==
                            MapType.SATELLITE
                        ) {
                            "MAP"
                        } else {
                            "SAT"
                        },
                    wide =
                        true,
                    onClick = {
                        mapType =
                            if (
                                mapType ==
                                MapType.SATELLITE
                            ) {
                                MapType.NORMAL
                            } else {
                                MapType.SATELLITE
                            }
                    }
                )
            }

            /*
             * Distance progress chip.
             */
            Surface(
                modifier = Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .padding(
                        12.dp
                    ),
                shape =
                    RoundedCornerShape(
                        16.dp
                    ),
                color =
                    Color(0xE6FFFFFF)
            ) {
                Column(
                    modifier =
                        Modifier.padding(
                            horizontal =
                                12.dp,
                            vertical =
                                9.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            2.dp
                        )
                ) {
                    Text(
                        text =
                            String.format(
                                Locale.US,
                                "%.2f / %.2f km",
                                visibleDistanceMetres /
                                        1_000.0,
                                exactTargetMetres /
                                        1_000.0
                            ),
                        color =
                            Color(0xFF0F172A),
                        fontSize =
                            13.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        text =
                            if (
                                roadError
                                    .isNotBlank()
                            ) {
                                roadError
                            } else {
                                title
                            },
                        color =
                            if (
                                roadError
                                    .isNotBlank()
                            ) {
                                Color(0xFFB91C1C)
                            } else {
                                Color(0xFF475569)
                            },
                        fontSize =
                            9.sp,
                        maxLines =
                            2
                    )
                }
            }
        }
    }
}

@Composable
private fun MoveMateMapControl(
    text: String,
    wide: Boolean =
        false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .then(
                if (
                    wide
                ) {
                    Modifier
                        .height(
                            40.dp
                        )
                } else {
                    Modifier
                        .size(
                            40.dp
                        )
                }
            )
            .clickable(
                onClick =
                    onClick
            ),
        shape =
            RoundedCornerShape(
                13.dp
            ),
        color =
            Color(0xEFFFFFFF),
        shadowElevation =
            5.dp
    ) {
        Box(
            modifier =
                Modifier.padding(
                    horizontal =
                        if (
                            wide
                        ) {
                            9.dp
                        } else {
                            0.dp
                        }
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    text,
                color =
                    Color(0xFF0F172A),
                fontSize =
                    if (
                        wide
                    ) {
                        10.sp
                    } else {
                        20.sp
                    },
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Creates a short local grid route when the road API is unavailable.
 *
 * The fallback is intentionally used only after the preferred road request
 * fails. It keeps the moving marker, travelled line, timer distance, pause and
 * resume behavior working instead of leaving the point frozen at Start.
 */
private fun moveMateBuildFallbackRoadRoute(
    origin: LatLng,
    targetDistanceMetres: Double,
    activityName: String
): List<LatLng> {
    if (
        targetDistanceMetres <=
        0.0
    ) {
        return listOf(
            origin
        )
    }

    /*
     * Shorter blocks for walking, medium blocks for running and longer blocks
     * for cycling. Alternating east/west rows resemble a city-road grid.
     */
    val horizontalBlockMetres =
        when {
            activityName.equals(
                "Walking",
                ignoreCase =
                    true
            ) ->
                110.0

            activityName.equals(
                "Cycling",
                ignoreCase =
                    true
            ) ->
                240.0

            else ->
                160.0
        }

    val verticalBlockMetres =
        when {
            activityName.equals(
                "Walking",
                ignoreCase =
                    true
            ) ->
                65.0

            activityName.equals(
                "Cycling",
                ignoreCase =
                    true
            ) ->
                130.0

            else ->
                90.0
        }

    val result =
        mutableListOf(
            origin
        )

    var current =
        origin

    var remaining =
        targetDistanceMetres

    var moveEast =
        true

    var rowsCreated =
        0

    while (
        remaining >
        0.05 &&
        rowsCreated <
        200
    ) {
        val horizontalDistance =
            min(
                horizontalBlockMetres,
                remaining
            )

        current =
            moveMateDestinationPoint(
                start =
                    current,
                distanceMetres =
                    horizontalDistance,
                bearingDegrees =
                    if (
                        moveEast
                    ) {
                        90.0
                    } else {
                        270.0
                    }
            )

        result.add(
            current
        )

        remaining -=
            horizontalDistance

        if (
            remaining <=
            0.05
        ) {
            break
        }

        val verticalDistance =
            min(
                verticalBlockMetres,
                remaining
            )

        /*
         * Move south between rows. This keeps the fallback from repeatedly
         * drawing over the same road segment.
         */
        current =
            moveMateDestinationPoint(
                start =
                    current,
                distanceMetres =
                    verticalDistance,
                bearingDegrees =
                    180.0
            )

        result.add(
            current
        )

        remaining -=
            verticalDistance

        moveEast =
            !moveEast

        rowsCreated++
    }

    return if (
        result.size >=
        2
    ) {
        result
    } else {
        listOf(
            origin,
            moveMateDestinationPoint(
                start =
                    origin,
                distanceMetres =
                    targetDistanceMetres,
                bearingDegrees =
                    90.0
            )
        )
    }
}

/**
 * Returns a point at the requested distance and bearing from the start point.
 */
private fun moveMateDestinationPoint(
    start: LatLng,
    distanceMetres: Double,
    bearingDegrees: Double
): LatLng {
    val angularDistance =
        distanceMetres /
                EARTH_RADIUS_METRES

    val bearingRadians =
        Math.toRadians(
            bearingDegrees
        )

    val startLatitude =
        Math.toRadians(
            start.latitude
        )

    val startLongitude =
        Math.toRadians(
            start.longitude
        )

    val endLatitude =
        asin(
            sin(
                startLatitude
            ) *
                    cos(
                        angularDistance
                    ) +
                    cos(
                        startLatitude
                    ) *
                    sin(
                        angularDistance
                    ) *
                    cos(
                        bearingRadians
                    )
        )

    val endLongitude =
        startLongitude +
                atan2(
                    sin(
                        bearingRadians
                    ) *
                            sin(
                                angularDistance
                            ) *
                            cos(
                                startLatitude
                            ),
                    cos(
                        angularDistance
                    ) -
                            sin(
                                startLatitude
                            ) *
                            sin(
                                endLatitude
                            )
                )

    return LatLng(
        Math.toDegrees(
            endLatitude
        ),
        Math.toDegrees(
            endLongitude
        )
    )
}

private fun moveMateTrimRoadToDistance(
    road: List<LatLng>,
    targetDistanceMetres: Double
): List<LatLng> {
    if (
        road.size <
        2 ||
        targetDistanceMetres <=
        0.0
    ) {
        return emptyList()
    }

    val result =
        mutableListOf(
            road.first()
        )

    var accumulated =
        0.0

    for (
    index in
    1 until
            road.size
    ) {
        val from =
            road[index - 1]

        val to =
            road[index]

        val segment =
            moveMateDistanceMetres(
                from,
                to
            )

        if (
            segment <=
            0.01
        ) {
            continue
        }

        if (
            accumulated +
            segment >=
            targetDistanceMetres
        ) {
            val remaining =
                targetDistanceMetres -
                        accumulated

            val fraction =
                (
                        remaining /
                                segment
                        )
                    .coerceIn(
                        0.0,
                        1.0
                    )

            result.add(
                moveMateInterpolateLatLng(
                    from,
                    to,
                    fraction
                )
            )

            return result
        }

        result.add(
            to
        )

        accumulated +=
            segment
    }

    /*
     * The route service should normally return enough road. If it returns a
     * shorter road, keep the valid road instead of inventing a line through
     * buildings or water.
     */
    return result
}

private fun moveMatePointAtRoadDistance(
    road: List<LatLng>,
    distanceMetres: Double
): LatLng? {
    if (
        road.isEmpty()
    ) {
        return null
    }

    if (
        road.size ==
        1 ||
        distanceMetres <=
        0.0
    ) {
        return road.first()
    }

    var accumulated =
        0.0

    for (
    index in
    1 until
            road.size
    ) {
        val from =
            road[index - 1]

        val to =
            road[index]

        val segment =
            moveMateDistanceMetres(
                from,
                to
            )

        if (
            accumulated +
            segment >=
            distanceMetres
        ) {
            val fraction =
                if (
                    segment <=
                    0.0
                ) {
                    0.0
                } else {
                    (
                            (
                                    distanceMetres -
                                            accumulated
                                    ) /
                                    segment
                            )
                        .coerceIn(
                            0.0,
                            1.0
                        )
                }

            return moveMateInterpolateLatLng(
                from,
                to,
                fraction
            )
        }

        accumulated +=
            segment
    }

    return road.last()
}

private fun moveMateRoadPrefixAtDistance(
    road: List<LatLng>,
    distanceMetres: Double
): List<LatLng> {
    if (
        road.isEmpty()
    ) {
        return emptyList()
    }

    if (
        road.size ==
        1 ||
        distanceMetres <=
        0.0
    ) {
        return listOf(
            road.first()
        )
    }

    val result =
        mutableListOf(
            road.first()
        )

    var accumulated =
        0.0

    for (
    index in
    1 until
            road.size
    ) {
        val from =
            road[index - 1]

        val to =
            road[index]

        val segment =
            moveMateDistanceMetres(
                from,
                to
            )

        if (
            accumulated +
            segment >=
            distanceMetres
        ) {
            val fraction =
                if (
                    segment <=
                    0.0
                ) {
                    0.0
                } else {
                    (
                            (
                                    distanceMetres -
                                            accumulated
                                    ) /
                                    segment
                            )
                        .coerceIn(
                            0.0,
                            1.0
                        )
                }

            val point =
                moveMateInterpolateLatLng(
                    from,
                    to,
                    fraction
                )

            if (
                result.last() !=
                point
            ) {
                result.add(
                    point
                )
            }

            return result
        }

        result.add(
            to
        )

        accumulated +=
            segment
    }

    return result
}

private fun moveMateInterpolateLatLng(
    from: LatLng,
    to: LatLng,
    fraction: Double
): LatLng {
    val safeFraction =
        fraction.coerceIn(
            0.0,
            1.0
        )

    return LatLng(
        from.latitude +
                (
                        to.latitude -
                                from.latitude
                        ) *
                safeFraction,
        from.longitude +
                (
                        to.longitude -
                                from.longitude
                        ) *
                safeFraction
    )
}

private fun moveMateDistanceMetres(
    first: LatLng,
    second: LatLng
): Double {
    val latitude1 =
        Math.toRadians(
            first.latitude
        )

    val latitude2 =
        Math.toRadians(
            second.latitude
        )

    val latitudeDifference =
        Math.toRadians(
            second.latitude -
                    first.latitude
        )

    val longitudeDifference =
        Math.toRadians(
            second.longitude -
                    first.longitude
        )

    val a =
        sin(
            latitudeDifference /
                    2.0
        ) *
                sin(
                    latitudeDifference /
                            2.0
                ) +
                cos(
                    latitude1
                ) *
                cos(
                    latitude2
                ) *
                sin(
                    longitudeDifference /
                            2.0
                ) *
                sin(
                    longitudeDifference /
                            2.0
                )

    val c =
        2.0 *
                atan2(
                    sqrt(
                        a
                    ),
                    sqrt(
                        1.0 -
                                a
                    )
                )

    return EARTH_RADIUS_METRES *
            c
}

private fun moveMateHasLocationPermission(
    context: Context
): Boolean {
    return ActivityCompat
        .checkSelfPermission(
            context,
            Manifest.permission
                .ACCESS_FINE_LOCATION
        ) ==
            PackageManager
                .PERMISSION_GRANTED ||
            ActivityCompat
                .checkSelfPermission(
                    context,
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ) ==
            PackageManager
                .PERMISSION_GRANTED
}

private fun moveMateIsEmulator(): Boolean {
    return (
            Build.FINGERPRINT
                .startsWith(
                    "generic"
                ) ||
                    Build.FINGERPRINT
                        .lowercase(
                            Locale.US
                        )
                        .contains(
                            "emulator"
                        ) ||
                    Build.MODEL
                        .lowercase(
                            Locale.US
                        )
                        .contains(
                            "emulator"
                        ) ||
                    Build.MODEL
                        .contains(
                            "Android SDK built for x86"
                        ) ||
                    Build.MANUFACTURER
                        .lowercase(
                            Locale.US
                        )
                        .contains(
                            "genymotion"
                        ) ||
                    Build.PRODUCT
                        .lowercase(
                            Locale.US
                        )
                        .contains(
                            "sdk"
                        )
            )
}
