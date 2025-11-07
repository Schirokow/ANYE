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
import androidx.compose.foundation.layout.fillMaxSize
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
            OpenStreetMapDisplay(
                eventLat = eventLat,
                eventLng = eventLng,
                eventName = eventName
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
                tint = eventOrange,
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
    eventLat: Double? = null,
    eventLng: Double? = null,
    eventName: String? = null
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var zoomLevel by remember { mutableStateOf(15.0) }

    // Wir speichern die Marker selbst, anstatt sie ständig neu zu erstellen.
    var eventMarker by remember { mutableStateOf<Marker?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }

    // 4. Erstelle einen GeoPoint für das Event, falls vorhanden
    val eventGeoPoint = remember(eventLat, eventLng) {
        if (eventLat != null && eventLng != null) {
            GeoPoint(eventLat, eventLng)
        } else {
            null
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
            controller.setZoom(zoomLevel)

            // 5. Setze den initialen Mittelpunkt
            // Wenn ein Event vorhanden ist, zentriere darauf.
            // Sonst zentriere auf einen Standardwert (z.B. Berlin),
            // der später vom User-Standort überschrieben wird.
            controller.setCenter(eventGeoPoint ?: GeoPoint(52.5200, 13.4050))
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
    LaunchedEffect(mapView, eventGeoPoint) {
        val localMapView = mapView ?: return@LaunchedEffect

        // 1. Event-Marker erstellen (falls vorhanden)
        eventGeoPoint?.let { eventPoint ->
            val marker = Marker(localMapView).apply {
                position = eventPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = eventName ?: "Event-Standort"
                snippet = "Entfernung wird berechnet..." // Initialer Text
                val icon = ContextCompat.getDrawable(context, R.drawable.ic_event_pin)
                icon?.let { setIcon(it) }

                // Click-Listener, damit der User es manuell öffnen/schließen kann
                setOnMarkerClickListener { m, _ ->
                    if (m.isInfoWindowShown) {
                        m.closeInfoWindow()
                    } else {
                        m.showInfoWindow()
                    }
                    true // Event konsumiert
                }
            }
            localMapView.overlays.add(marker)
            marker.showInfoWindow() // Initial einmal anzeigen
            eventMarker = marker // Im State speichern
        }

        // 2. User-Marker erstellen (unsichtbar, bis Standort da ist)
        val marker = Marker(localMapView).apply {
            position = GeoPoint(0.0, 0.0) // Platzhalter
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Aktuelle Position"
            setEnabled(false) // Verstecken, bis wir echten Standort haben
        }
        localMapView.overlays.add(marker)
        userMarker = marker // Im State speichern

        localMapView.invalidate()
    }


    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var lastLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Dieser Effekt läuft, JEDES MAL wenn 'lastLocation' sich ändert.
    // Er aktualisiert die bestehenden Marker.
    LaunchedEffect(lastLocation, eventMarker, userMarker) {
        val localMapView = mapView ?: return@LaunchedEffect

        // 1. User-Marker Position aktualisieren
        userMarker?.let { marker ->
            lastLocation?.let {
                marker.position = it // Position aktualisieren
                marker.setEnabled(true) // Sichtbar machen

                // Zum User zoomen, WENN kein Event da ist
                if (eventGeoPoint == null && !localMapView.isAnimating) {
                    localMapView.controller.animateTo(it)
                }
            }
        }

        // 2. Event-Marker Snippet (Entfernung) aktualisieren
        eventMarker?.let { marker ->
            val distanceString = if (lastLocation != null && eventGeoPoint != null) {
                val distanceInMeters = eventGeoPoint.distanceToAsDouble(lastLocation!!)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    zoomLevel = (zoomLevel + 1).coerceAtMost(30.0)
                    mapView?.controller?.setZoom(zoomLevel)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF29719E),  // EVOO Blauton Hintergrundfarbe
                    contentColor = Color.White      // Text-/Symbolfarbe
                )
            ) {
                Text("+")
            }
            Button(
                onClick = {
                    zoomLevel = (zoomLevel - 1).coerceAtLeast(10.0)
                    mapView?.controller?.setZoom(zoomLevel)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF29719E),  // EVOO Blauton Hintergrundfarbe
                    contentColor = Color.White      // Text-/Symbolfarbe
                )
            ) {
                Text("-")
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