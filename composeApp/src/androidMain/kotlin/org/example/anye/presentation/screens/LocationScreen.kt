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
    LaunchedEffect(mapView, eventGeoPoint) {
        if (mapView != null && eventGeoPoint != null) {
            mapView?.controller?.animateTo(eventGeoPoint)
            mapView?.controller?.setZoom(16.0) // Setze einen guten Zoom-Level für ein Event
        }
    }


    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var lastLocation by remember { mutableStateOf<GeoPoint?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Kartenansicht
        mapView?.let { mv ->
            AndroidView(
                factory = { mv },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // 7. Update-Logik anpassen
                    view.controller.setZoom(zoomLevel) // Zoom-Level beibehalten
                    view.overlays.removeIf { true } // Alle Marker löschen

                    // 7a. Event-Marker hinzufügen (falls vorhanden)
                    eventGeoPoint?.let {
                        val eventMarker = Marker(view).apply {
                            position = it
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            // 5. Titel und Icon setzen!
                            title = eventName ?: "Event-Standort"

                            // 6. Lade das Drawable
                            val icon = ContextCompat.getDrawable(context, R.drawable.ic_event_pin)
                            icon?.let { setIcon(it) }

                            // 7. Optional: Info-Fenster sofort anzeigen
                            showInfoWindow()
                        }
                        view.overlays.add(eventMarker)
                    }


                    // 7b. User-Positions-Marker hinzufügen (falls Berechtigung erteilt)
                    if (locationPermissionState.status.isGranted) {
                        lastLocation?.let {
                            Marker(view).apply {
                                position = it
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Aktuelle Position"
                            }.also { marker ->
                                view.overlays.add(marker)
                            }

                            // 7c. Nur zur User-Position animieren, WENN KEIN Event angezeigt wird
                            if (eventGeoPoint == null && !view.isAnimating) {
                                view.controller.animateTo(it)
                            }
                        }
                    }
                    view.invalidate() // Wichtig, um die Overlays neu zu zeichnen
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


val eventOrange = Color(0xFFE64A19)