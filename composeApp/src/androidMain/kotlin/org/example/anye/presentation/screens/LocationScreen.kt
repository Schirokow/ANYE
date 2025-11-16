package org.example.anye.presentation.screens


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.evoo.ui.theme.colorthemetype.BottomDarkBlue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.anye.R
import org.example.anye.data.MapDataHolder
import org.example.anye.presentation.map.CustomMarkerInfoWindow
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.ui.menu.AnyeBottomBar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import kotlin.math.ln
import kotlin.math.min


// Eine Klasse, um Lat/Lng/Name zu bündeln
private data class MapEventInfo(
    val lat: Double,
    val lng: Double,
    val name: String
) {
    fun toGeoPoint(): GeoPoint = GeoPoint(lat, lng)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen(
    navController: NavController,
    eventLat: Double? = null, // 1. Empfange die Parameter
    eventLng: Double? = null,
    eventName: String? = null
) {

    val snackbarHostState = remember { SnackbarHostState() }

    // Scaffold als Hauptcontainer verwenden
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
//        containerColor = AccentColor,
    ) { paddingValues -> // paddingValues berücksichtigt die Systemleisten

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
//                .background(BackgroundColor)
        ) {

            // 2. Übergebe die Parameter an die Karten-Composable
            // Die Logik, ob Multi-Event geladen wird,
            // steckt jetzt in OpenStreetMapDisplay.
            OpenStreetMapDisplay(
                singleEventLat = eventLat,
                singleEventLng = eventLng,
                singleEventName = eventName
            )

            // Auth-Status oben rechts
            AuthStatusIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            )

            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = BottomDarkBlue,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(4.dp)
                    .size(34.dp)
                    .clickable {
                        Log.d("LocationScreen", "Navigation: Returning to previous screen")
                        navController.popBackStack()
                    }
            )

            AnyeBottomBar(navController)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OpenStreetMapDisplay(
    singleEventLat: Double? = null,
    singleEventLng: Double? = null,
    singleEventName: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var zoomLevel by remember { mutableStateOf(15.0) }


    // Es startet leer und wird vom LaunchedEffect befüllt.
    var mapEvents by remember { mutableStateOf<List<MapEventInfo>>(emptyList()) }

    // --- State für Marker UND ihre zugehörigen InfoWindows ---
    // Wir müssen jedes Fenster manuell speichern, um es zu verwalten.
    var markerWindowMap by remember { mutableStateOf<Map<Marker, InfoWindow>>(emptyMap()) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    var userInfoWindow by remember { mutableStateOf<InfoWindow?>(null) } // Separates Fenster für User

    // --- State, um die Start-Animation nur einmal auszuführen ---
    var hasAnimatedOnLoad by remember { mutableStateOf(false) }

    // --- Zwei separate States für Standort ---
    var firstLocation by remember { mutableStateOf<GeoPoint?>(null) } // Nur für die Start-Animation
    var lastLocation by remember { mutableStateOf<GeoPoint?>(null) }  // Für Live-Entfernungs-Updates

    // --- 2: Datenverarbeitung in einen LaunchedEffect verschieben ---
    LaunchedEffect(singleEventLat, singleEventLng, singleEventName) {
        if (singleEventLat != null && singleEventLng != null && singleEventName != null) {
            // 1. SINGLE-EVENT-MODUS (schnell, kein Hintergrund-Thread nötig)
            Log.d("OpenStreetMapDisplay", "Modus: Einzel-Event")
            mapEvents = listOf(MapEventInfo(singleEventLat, singleEventLng, singleEventName))
        } else {
            // 2. MULTI-EVENT-MODUS (potenziell langsam)
            Log.d("OpenStreetMapDisplay", "Modus: Multi-Event")
            val eventsFromHolder = MapDataHolder.events
            MapDataHolder.events = emptyList() // Holder leeren

            if (eventsFromHolder.isNotEmpty()) {

                // --- 3: Arbeit in einen HINTERGRUND-THREAD verlagern ---
                val processedEvents = withContext(Dispatchers.Default) {
                    // Diese teure Operation läuft jetzt im Hintergrund
                    // und blockiert NICHT die UI.
                    eventsFromHolder.mapNotNull { event ->
                        val venue = event._embedded?.venues?.firstOrNull()
                        val lat = venue?.location?.latitude?.toDoubleOrNull()
                        val lng = venue?.location?.longitude?.toDoubleOrNull()

                        if (lat != null && lng != null) {
                            MapEventInfo(lat, lng, event.name)
                        } else {
                            null // Events ohne Ort überspringen
                        }
                    }
                }

                // --- Update des States (zurück auf dem Main Thread) ---
                mapEvents = processedEvents
                Log.d(
                    "OpenStreetMapDisplay",
                    "Multi-Event Verarbeitung fertig: ${processedEvents.size} Events."
                )
            }
        }
    }

    // OSMDroid Konfiguration
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        }
        mapView = MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 10.0
            maxZoomLevel = 30.0
            // Setze einen sehr weiten Zoom, damit die Animation gut sichtbar ist
            controller.setZoom(5.0)
            controller.setCenter(GeoPoint(51.1657, 10.4515))
        }
        Log.d("OpenStreetMapDisplay", "MapView initialisiert")
    }

    // Dieser Effekt erstellt nur noch die Marker, startet aber KEINE Animation.
    LaunchedEffect(mapView, mapEvents) {
        val localMapView = mapView ?: return@LaunchedEffect

        // 1. Alle alten Marker und InfoWindows restlos entfernen
        markerWindowMap.values.forEach { it.close() } // Alle Fenster schließen
        localMapView.overlays.removeAll(markerWindowMap.keys.toSet()) // Alle Marker entfernen

        // 2. NEUE Marker UND InfoWindows erstellen
        val newMarkerWindowMap = mutableMapOf<Marker, InfoWindow>()

        mapEvents.forEach { mapEvent ->
            // Benutze den Standard-Konstruktor
            val infoWindow = CustomMarkerInfoWindow(localMapView)

            val marker = Marker(localMapView).apply {
                position = mapEvent.toGeoPoint()
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = mapEvent.name
                snippet = "Entfernung wird berechnet..."
                val icon = ContextCompat.getDrawable(context, R.drawable.ic_event_pin)
                icon?.let { setIcon(it) }
                setInfoWindow(infoWindow)

                setOnMarkerClickListener { m, _ ->
                    val tag = (m.relatedObject as? MutableMap<String, Any?>) ?: mutableMapOf()
                    if (m.isInfoWindowShown) {
                        tag["manuallyClosed"] = true
                        m.closeInfoWindow()
                    } else {
                        tag["manuallyClosed"] = false
                        m.showInfoWindow()
                    }
                    m.relatedObject = tag
                    true
                }
            }
            newMarkerWindowMap[marker] = infoWindow
            localMapView.overlays.add(marker) // Marker zur Karte hinzufügen

            // Alle Event-Fenster standardmäßig öffnen
            marker.showInfoWindow()
        }

        markerWindowMap = newMarkerWindowMap // Neuen State speichern

        // 3. User-Marker (erstellen, falls nicht vorhanden)
        if (userMarker == null) {
            val uMarker = Marker(localMapView).apply {
                position = GeoPoint(0.0, 0.0)
                title = ""
                setEnabled(false)
            }
            val uInfoWindow = CustomMarkerInfoWindow(localMapView)
            uMarker.setInfoWindow(uInfoWindow)
            uMarker.setOnMarkerClickListener { m, _ ->
                if (m.isInfoWindowShown) m.closeInfoWindow() else m.showInfoWindow()
                true
            }
            userMarker = uMarker
            userInfoWindow = uInfoWindow
            localMapView.overlays.add(uMarker)
        }

        localMapView.invalidate()
    }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var hasShownUserInfo by remember { mutableStateOf(false) }
    // Dieser Effekt läuft, JEDES MAL wenn 'lastLocation' sich ändert.
    // Er aktualisiert die bestehenden Marker.
    LaunchedEffect(lastLocation, markerWindowMap, userMarker) {
        val localMapView = mapView ?: return@LaunchedEffect

        // User-Marker Position aktualisieren
        userMarker?.let { marker ->
            lastLocation?.let {
                marker.position = it
                marker.setEnabled(true)
                marker.snippet = "Aktuelle Position"

                if (!hasShownUserInfo) {
                    marker.showInfoWindow()
                    fadeInInfoWindow(userInfoWindow)
                    hasShownUserInfo = true
                }
            }
        }

        // Entfernung für Event-Marker aktualisieren
        markerWindowMap.forEach { (marker, infoWindow) ->
            val distanceString = when {
                lastLocation != null -> {
                    val distanceInMeters = marker.position.distanceToAsDouble(lastLocation!!)
                    "Entfernung: ${formatDistance(distanceInMeters.toFloat())}"
                }

                locationPermissionState.status.isGranted -> "Entfernung wird berechnet..."
                else -> "Standortberechtigung fehlt"
            }

            marker.snippet = distanceString

            // Nur aktualisieren, wenn Fenster offen ist:
            if (marker.isInfoWindowShown) {
                marker.closeInfoWindow()
                marker.showInfoWindow()
            }
        }

        // Nur beim ersten erfolgreichen Standort-Fix ALLE öffnen
        if (lastLocation != null) {
            markerWindowMap.forEach { (marker, infoWindow) ->
                // Nur öffnen, wenn:
                // a) noch nie geöffnet wurde
                // b) vom Nutzer nicht manuell geschlossen wurde
                val tag = marker.relatedObject as? MutableMap<String, Any?> ?: mutableMapOf()
                val manuallyClosed = tag["manuallyClosed"] as? Boolean ?: false
                val alreadyOpened = tag["alreadyOpened"] as? Boolean ?: false

                if (!manuallyClosed && !alreadyOpened) {
                    marker.showInfoWindow()
                    tag["alreadyOpened"] = true
                    marker.relatedObject = tag
                }
            }
        }

        localMapView.invalidate()
    }

    // Follow-Modus: Karte folgt automatisch dem User
    var isFollowingUser by remember { mutableStateOf(MapDataHolder.shouldFollowUser) }

    // --- PERMANENT SMOOTH FOLLOW MODE ---
    var lastAnimatedCenter by remember { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(lastLocation, isFollowingUser) {
        val map = mapView ?: return@LaunchedEffect
        val userPoint = lastLocation ?: return@LaunchedEffect
        MapDataHolder.shouldFollowUser = isFollowingUser

        if (!isFollowingUser) return@LaunchedEffect // Kein Follow, wenn deaktiviert

        val shouldAnimate = lastAnimatedCenter == null ||
                userPoint.distanceToAsDouble(lastAnimatedCenter) > 8.0 // ~8m Bewegung

        if (shouldAnimate) {
            smoothFollowTo(map, lastAnimatedCenter, userPoint)
            lastAnimatedCenter = userPoint
        }
    }

    // --- START-ANIMATION: Korrigierte Version ---
    LaunchedEffect(mapView, mapEvents.isNotEmpty(), hasAnimatedOnLoad) {
        val localMapView = mapView ?: return@LaunchedEffect
        if (hasAnimatedOnLoad || mapEvents.isEmpty()) return@LaunchedEffect // Wichtig: Nur animieren wenn Events da sind

        Log.d("OpenStreetMapDisplay", "Start-Animation: Prüfe MapView Größe...")

        // Warte auf MapView Messung mit Timeout
        var waitCount = 0
        while ((localMapView.width <= 0 || localMapView.height <= 0) && waitCount < 50) {
            kotlinx.coroutines.delay(50)
            waitCount++
            Log.d("OpenStreetMapDisplay", "Warte auf MapView Messung... $waitCount")
        }

        if (localMapView.width <= 0 || localMapView.height <= 0) {
            Log.e("OpenStreetMapDisplay", "MapView wurde nicht gemessen - Animation abgebrochen")
            return@LaunchedEffect
        }

        // Zusätzliche Verzögerung für Map-Rendering
        kotlinx.coroutines.delay(300)

        Log.d("OpenStreetMapDisplay", "Start-Animation wird ausgeführt für ${mapEvents.size} Events")

        try {
            when {
                mapEvents.size == 1 -> {
                    val point = mapEvents.first().toGeoPoint()
                    Log.d("OpenStreetMapDisplay", "Zoome zu Einzel-Event: $point")
                    animateZoomOutThenIn(localMapView, point, targetZoom = 16.8, durationIn = 1600L)
                }
                mapEvents.size > 1 -> {
                    val box = BoundingBox.fromGeoPoints(mapEvents.map { it.toGeoPoint() }).increaseByScale(1.4f)
                    Log.d("OpenStreetMapDisplay", "Zoome zu Multi-Events: $box")
                    animateToBoundingBoxSmoothly(localMapView, box, duration = 2600L)
                }
            }

            // Erst NACH erfolgreicher Animation auf true setzen
            hasAnimatedOnLoad = true
            Log.d("OpenStreetMapDisplay", "Start-Animation erfolgreich abgeschlossen")

        } catch (e: Exception) {
            Log.e("OpenStreetMapDisplay", "Fehler bei Start-Animation: ${e.message}")
            hasAnimatedOnLoad = true // Trotzdem auf true setzen, um Endlosschleife zu vermeiden
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Kartenansicht
        mapView?.let { mv ->
            AndroidView(
                factory = { mv },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // Die 'update'-Logik wird nur noch für Dinge wie Zoom
                    // ODER für nicht-state-basierte Änderungen benötigt.
                    if (view.zoomLevelDouble != zoomLevel) {
                        view.controller.setZoom(zoomLevel)
                    }
                }
            )
        }

        // Benutzerdefinierte Zoom-Buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    zoomLevel = (zoomLevel + 1).coerceAtMost(30.0)
                    mapView?.controller?.setZoom(zoomLevel)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF29719E).copy(alpha = 0.65f),  // Blauton Hintergrundfarbe
                    contentColor = Color.White      // Text-/Symbolfarbe
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = "Plus"
                )
            }
            Button(
                onClick = {
                    zoomLevel = (zoomLevel - 1).coerceAtLeast(10.0)
                    mapView?.controller?.setZoom(zoomLevel)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF29719E).copy(alpha = 0.65f),  // Blauton Hintergrundfarbe
                    contentColor = Color.White      // Text-/Symbolfarbe
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = "Minus"
                )
            }

            Spacer(modifier = Modifier.height(250.dp))

            // --- Event-Zentrierungs-Button ---
            Button(
                onClick = {
                    coroutineScope.launch {
                        isFollowingUser = false // Follow ausschalten

                        val localMapView = mapView ?: return@launch

                        if (mapEvents.size == 1) {
                            val eventPoint = mapEvents.first().toGeoPoint()
                            animateZoomOutThenIn(localMapView, eventPoint)
                        } else if (mapEvents.size > 1) {
                            val boundingBox = BoundingBox.fromGeoPoints(mapEvents.map { it.toGeoPoint() })
                            animateToBoundingBoxSmoothly(localMapView, boundingBox)
                        }
                    }

                },
                // Deaktiviere den Button, wenn kein Event-Punkt vorhanden ist
                enabled = mapEvents.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF29719E).copy(alpha = 0.65f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = "Event zentrieren"
                )
            }

            // --- User-Zentrierungs-Button ---
            Button(
                onClick = {
                    coroutineScope.launch {
                        isFollowingUser = true //Follow einschalten
                        MapDataHolder.shouldFollowUser = true // Synchron bleiben
                        lastLocation?.let { point ->
                            val map = mapView ?: return@launch
                            animateZoomOutThenIn(map, point, targetZoom = 17.0)
                        }
                    }
                },
                // Deaktiviere den Button, bis der User-Standort gefunden wurde
                enabled = lastLocation != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF29719E).copy(alpha = 0.65f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Auf mich zentrieren"
                )
            }
        }

    }

    if (locationPermissionState.status.isGranted) {
        LocationHandler { location ->
            val newGeoPoint = GeoPoint(location.latitude, location.longitude)
            // Setze den 'firstLocation'-Trigger nur, wenn er noch nicht gesetzt ist
            if (firstLocation == null) {
                firstLocation = newGeoPoint
            }
            // Aktualisiere 'lastLocation' immer für Live-Distanzen
            lastLocation = newGeoPoint
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Standortberechtigung benötigt")
            Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                Text("Berechtigung anfordern")
            }
        }
    }
}

@Composable
fun LocationHandler(onLocationUpdate: (Location) -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    DisposableEffect(Unit) {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let(onLocationUpdate)
            }
        }

        val locationRequest = LocationRequest.create().apply {
            // Korrektur: Verwenden Sie die Eigenschaften direkt statt der statischen Methoden
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}


private fun formatDistance(meters: Float): String {
    return if (meters < 1000) {
        // Z.B. "750 m"
        "${meters.toInt()} m"
    } else {
        // Z.B. "1.2 km"
        val kilometers = meters / 1000.0
        // Benutze DecimalFormat für eine saubere Formatierung (z.B. 1,2 statt 1.2345)
        val df = java.text.DecimalFormat("#.#")
        "${df.format(kilometers)} km"
    }
}

fun fadeInInfoWindow(infoWindow: InfoWindow?) {
    val view = infoWindow?.view ?: return
    view.alpha = 0f
    view.post {
        view.animate()
            .alpha(1f)
            .setDuration(600)
            .start()
    }
}

fun smoothInterpolator(t: Float): Double {
    // Easing: Beschleunigt am Anfang, verlangsamt am Ende
    return (1 - kotlin.math.cos(t * Math.PI)) / 2.0f
}

fun MapView.getBoundingBoxZoom(boundingBox: BoundingBox): Double {
    val width = this.width.toDouble()
    val height = this.height.toDouble()
    if (width == 0.0 || height == 0.0) return 12.0 // Fallback

    val latSpan = boundingBox.latNorth - boundingBox.latSouth
    val lonSpan = boundingBox.lonEast - boundingBox.lonWest

    if (latSpan <= 0 || lonSpan <= 0) return 12.0

    val worldSize = 256.0
    val zoomX = ln(360.0 * width / (lonSpan * worldSize)) / ln(2.0)
    val zoomY = ln(180.0 * height / (latSpan * worldSize)) / ln(2.0)

    return min(zoomX, zoomY).coerceIn(this.minZoomLevel, this.maxZoomLevel) - 0.5 // Etwas rauszoomen für Padding
}

suspend fun animateZoomOutThenIn(
    mapView: MapView,
    target: GeoPoint,
    targetZoom: Double = 16.8,
    zoomOutFactor: Double = 0.4, // Stärker rauszoomen für dramatischere Animation
    durationOut: Long = 800L,
    durationIn: Long = 1400L
) {
    val startZoom = mapView.zoomLevelDouble
    val zoomOutLevel = (startZoom * zoomOutFactor).coerceAtLeast(mapView.minZoomLevel)

    // Erst schnell rauszoomen
    animateZoomSmoothly(mapView, startZoom, zoomOutLevel, durationOut)
    kotlinx.coroutines.delay(200)

    // Zum Ziel zentrieren
    withContext(Dispatchers.Main) {
        mapView.controller.animateTo(target)
    }
    kotlinx.coroutines.delay(100)

    // Sanft reinzoomen
    animateZoomSmoothly(mapView, zoomOutLevel, targetZoom, durationIn)
}

suspend fun animateZoomSmoothly(
    mapView: MapView,
    startZoom: Double,
    endZoom: Double,
    duration: Long = 1200L
) {
    val steps = 60
    val stepDelay = duration / steps
    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val interpolatedZoom = startZoom + (endZoom - startZoom) * smoothInterpolator(t)
        withContext(Dispatchers.Main) {
            mapView.controller.setZoom(interpolatedZoom)
            mapView.invalidate()
        }
        kotlinx.coroutines.delay(stepDelay)
    }
}

suspend fun animateToBoundingBoxSmoothly(
    mapView: MapView,
    boundingBox: BoundingBox,
    duration: Long = 1800L
) {
    val startZoom = mapView.zoomLevelDouble
    val startCenter = mapView.mapCenter

    // Berechne den Ziel-Zoom für die BoundingBox
    val targetZoom = mapView.getBoundingBoxZoom(boundingBox)
    val targetCenter = boundingBox.centerWithDateLine

    // Gleiche Zoom-Out-Then-In Animation wie bei einzelnen Events
    val zoomOutFactor = 0.4
    val zoomOutLevel = (startZoom * zoomOutFactor).coerceAtLeast(mapView.minZoomLevel)

    // 1. Erst rauszoomen
    animateZoomSmoothly(mapView, startZoom, zoomOutLevel, duration / 3)
    kotlinx.coroutines.delay(200)

    // 2. Zum Ziel zentrieren
    withContext(Dispatchers.Main) {
        mapView.controller.animateTo(targetCenter)
    }
    kotlinx.coroutines.delay(100)

    // 3. Sanft reinzoomen auf den berechneten Zoom-Level
    animateZoomSmoothly(mapView, zoomOutLevel, targetZoom, duration * 2 / 3)

    // Finale Korrektur
    withContext(Dispatchers.Main) {
        mapView.controller.setCenter(targetCenter)
        mapView.controller.setZoom(targetZoom)
    }
}

suspend fun smoothFollowTo(
    mapView: MapView,
    from: GeoPoint?,
    to: GeoPoint,
    duration: Long = 900L
) {
    val start = from ?: mapView.mapCenter as GeoPoint
    val steps = 30
    val delayPerStep = duration / steps

    val dLat = (to.latitude - start.latitude) / steps
    val dLng = (to.longitude - start.longitude) / steps

    for (i in 1..steps) {
        val lat = start.latitude + dLat * i
        val lng = start.longitude + dLng * i
        val intermediate = GeoPoint(lat, lng)

        withContext(Dispatchers.Main) {
            mapView.controller.setCenter(intermediate)
            mapView.invalidate()
        }
        kotlinx.coroutines.delay(delayPerStep)
    }
}

fun BoundingBox.increaseByScale(scale: Float): BoundingBox {
    val latCenter = (latNorth + latSouth) / 2
    val lonCenter = (lonEast + lonWest) / 2

    val newLatSpan = (latNorth - latSouth) * scale
    val newLonSpan = (lonEast - lonWest) * scale

    return BoundingBox(
        latCenter + newLatSpan / 2,
        lonCenter + newLonSpan / 2,
        latCenter - newLatSpan / 2,
        lonCenter - newLonSpan / 2
    )
}
