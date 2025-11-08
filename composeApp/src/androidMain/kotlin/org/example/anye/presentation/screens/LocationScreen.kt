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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.example.anye.R
import org.example.anye.ui.components.AuthStatusIndicator
import org.example.anye.ui.menu.AnyeBottomBar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import com.example.evoo.ui.theme.colorthemetype.BottomDarkBlue
import org.example.anye.data.MapDataHolder // neuer Holder
import org.osmdroid.util.BoundingBox // Für Auto-Zoom
import java.text.DecimalFormat
import org.example.anye.data.TicketmasterEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


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
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var zoomLevel by remember { mutableStateOf(15.0) }

    // --- Event-Daten vorbereiten ---
//    val mapEvents = remember(singleEventLat, singleEventLng, singleEventName) {
//        if (singleEventLat != null && singleEventLng != null && singleEventName != null) {
//            // 1. SINGLE-EVENT-MODUS (von ContentDetailScreen)
//            Log.d("OpenStreetMapDisplay", "Modus: Einzel-Event")
//            listOf(MapEventInfo(singleEventLat, singleEventLng, singleEventName))
//        } else {
//            // 2. MULTI-EVENT-MODUS (von HomeScreen)
//            Log.d("OpenStreetMapDisplay", "Modus: Multi-Event")
//            val eventsFromHolder = MapDataHolder.events
//            // WICHTIG: Holder leeren, damit die Daten nicht alt angezeigt werden
//            MapDataHolder.events = emptyList()
//
//            eventsFromHolder.mapNotNull { event ->
//                val venue = event._embedded?.venues?.firstOrNull()
//                val lat = venue?.location?.latitude?.toDoubleOrNull()
//                val lng = venue?.location?.longitude?.toDoubleOrNull()
//
//                if (lat != null && lng != null) {
//                    MapEventInfo(lat, lng, event.name)
//                } else {
//                    null // Events ohne Ort überspringen
//                }
//            }
//        }
//    }

    // Es startet leer und wird vom LaunchedEffect befüllt.
    var mapEvents by remember { mutableStateOf<List<MapEventInfo>>(emptyList()) }


    // --- Marker-States ---
    var eventMarkers by remember { mutableStateOf<List<Marker>>(emptyList()) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }

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
                Log.d("OpenStreetMapDisplay", "Multi-Event Verarbeitung fertig: ${processedEvents.size} Events.")
            }
        }
    }

    // Primärer Punkt zum Zentrieren (oder Berlin-Fallback)
    val primaryGeoPoint = remember(mapEvents) {
        mapEvents.firstOrNull()?.toGeoPoint() ?: GeoPoint(52.5200, 13.4050)
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
            controller.setZoom(zoomLevel)

            // 5. Setze den initialen Mittelpunkt
            // Wenn ein Event vorhanden ist, zentriere darauf.
            // Sonst zentriere auf einen Standardwert (z.B. Berlin),
            // der später vom User-Standort überschrieben wird.
            controller.setCenter(GeoPoint(primaryGeoPoint))
        }
    }

    // 6. Zentriere die Karte auf das Event (nur einmal, wenn die MapView bereit ist)
//    LaunchedEffect(mapView, eventGeoPoint) {
//        if (mapView != null && eventGeoPoint != null) {
//            mapView?.controller?.animateTo(eventGeoPoint)
//            mapView?.controller?.setZoom(16.0) // Setze einen guten Zoom-Level für ein Event
//        }
//    }

    // Dieser Effekt läuft, sobald die MapView bereit ist, und erstellt die Marker.
    LaunchedEffect(mapView, mapEvents) {
        val localMapView = mapView ?: return@LaunchedEffect

        // 1. Alte Event-Marker von der Karte entfernen
        eventMarkers.forEach { it.remove(localMapView) }

        // 2. NEUE Event-Marker erstellen
        val newMarkers = mapEvents.map { mapEvent ->
            Marker(localMapView).apply {
                position = mapEvent.toGeoPoint()
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = mapEvent.name
                snippet = "Entfernung wird berechnet..."
                val icon = ContextCompat.getDrawable(context, R.drawable.ic_event_pin)
                icon?.let { setIcon(it) }

                setOnMarkerClickListener { m, _ ->
                    if (m.isInfoWindowShown) m.closeInfoWindow() else m.showInfoWindow()
                    true
                }
            }
        }
        localMapView.overlays.addAll(newMarkers)

        // Zeige Info-Fenster, WENN es nur ein Event ist
        if (newMarkers.size == 1) {
            newMarkers.first().showInfoWindow()
            // Zentriere auf dieses eine Event
            localMapView.controller.animateTo(newMarkers.first().position)
            localMapView.controller.setZoom(16.0)
        }

        eventMarkers = newMarkers // Neue Marker im State speichern

        // 3. User-Marker (nur einmal erstellen)
        if (userMarker == null) {
            userMarker = Marker(localMapView).apply {
                position = GeoPoint(0.0, 0.0)
                title = "Aktuelle Position"
                setEnabled(false)
            }
            localMapView.overlays.add(userMarker)
        }

        // --- Auto-Zoom für alle Marker ---
        if (mapEvents.size > 1) {
            // Erstelle eine BoundingBox, die alle Punkte enthält
            val boundingBox = BoundingBox.fromGeoPoints(mapEvents.map { it.toGeoPoint() })
            // Zoome auf diese Box mit 100 Pixeln Rand
            localMapView.post {
                localMapView.zoomToBoundingBox(boundingBox, true, 100)
            }
        }

        localMapView.invalidate()
    }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    var lastLocation by remember { mutableStateOf<GeoPoint?>(null) }


    // Dieser Effekt läuft, JEDES MAL wenn 'lastLocation' sich ändert.
    // Er aktualisiert die bestehenden Marker.
    LaunchedEffect(lastLocation, eventMarkers, userMarker) {
        val localMapView = mapView ?: return@LaunchedEffect

        // 1. User-Marker Position aktualisieren
        userMarker?.let { marker ->
            lastLocation?.let {
                marker.position = it // Position aktualisieren
                marker.setEnabled(true) // Sichtbar machen

                // Nur zum User zoomen, wenn KEINE Events geladen sind
                if (mapEvents.isEmpty() && !localMapView.isAnimating) {
                    localMapView.controller.animateTo(it)
                }
            }
        }

        // 2. Alle Event-Marker Snippet (Entfernung) aktualisieren
        eventMarkers.forEach { marker ->
            val distanceString = if (lastLocation != null) {
                val distanceInMeters = marker.position.distanceToAsDouble(lastLocation!!)
                "Entfernung: ${formatDistance(distanceInMeters.toFloat())}"
            } else if (locationPermissionState.status.isGranted) {
                "Entfernung wird berechnet..."
            } else {
                "Standortberechtigung fehlt"
            }

            // Den Text im Marker-Objekt aktualisieren
            marker.snippet = distanceString

            // --- DAS IST DIE KERNLOGIK DEINER ANFRAGE ---
            // Wenn das Fenster gerade vom User offen gehalten wird,
            // erzwinge eine Aktualisierung, indem du es schließt und neu öffnest.
            if (marker.isInfoWindowShown) {
                marker.closeInfoWindow()
                marker.showInfoWindow()
            }
            // Wenn das Fenster geschlossen ist (isInfoWindowShown == false),
            // passiert hier nichts. Das Snippet wird "leise" im Hintergrund
            // aktualisiert und ist korrekt, wenn der User es das nächste Mal öffnet.
        }

        localMapView.invalidate() // Karte neu zeichnen, um Änderungen anzuzeigen
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
                    if (mapEvents.size > 1) {
                        // Bei vielen Events, zoome auf alle
                        val boundingBox = BoundingBox.fromGeoPoints(mapEvents.map { it.toGeoPoint() })
                        mapView?.zoomToBoundingBox(boundingBox, true, 100)
                    } else if (mapEvents.size == 1) {
                        // Bei einem Event, zentriere darauf
                        mapView?.controller?.animateTo(mapEvents.first().toGeoPoint())
                        mapView?.controller?.setZoom(16.0)
                        zoomLevel = 16.0
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
                    lastLocation?.let { point ->
                        mapView?.controller?.animateTo(point)
                        // Setze auch den Zoom für eine gute Ansicht
                        mapView?.controller?.setZoom(16.0)
                        zoomLevel = 16.0 // Zoom-Level im State aktualisieren
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
            lastLocation = GeoPoint(location.latitude, location.longitude)
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



val eventOrange = Color(0xFFE64A19)