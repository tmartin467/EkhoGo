package com.example.ekhogo.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

fun getMarkerColor(category: String): Float {
    return when (category) {
        "Campus Dining" -> BitmapDescriptorFactory.HUE_ORANGE
        "Academic Building" -> BitmapDescriptorFactory.HUE_RED
        "Student Housing" -> BitmapDescriptorFactory.HUE_VIOLET
        "Student Services" -> BitmapDescriptorFactory.HUE_AZURE
        else -> BitmapDescriptorFactory.HUE_ROSE
    }
}

@Composable
fun CampusMap(
    locations: List<CampusLocation>,
    cameraPositionState: CameraPositionState,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val darkMapStyle = """
        [
          {
            "elementType": "geometry",
            "stylers": [{ "color": "#16181C" }]
          },
          {
            "elementType": "labels.text.fill",
            "stylers": [{ "color": "#E7E9EA" }]
          },
          {
            "elementType": "labels.text.stroke",
            "stylers": [{ "color": "#0F0F0F" }]
          },
          {
            "featureType": "road",
            "elementType": "geometry",
            "stylers": [{ "color": "#2F3336" }]
          },
          {
            "featureType": "road",
            "elementType": "labels.text.fill",
            "stylers": [{ "color": "#C9D1D9" }]
          },
          {
            "featureType": "water",
            "elementType": "geometry",
            "stylers": [{ "color": "#000000" }]
          },
          {
            "featureType": "poi",
            "elementType": "geometry",
            "stylers": [{ "color": "#24262B" }]
          },
          {
            "featureType": "poi.park",
            "elementType": "geometry",
            "stylers": [{ "color": "#1F2A24" }]
          },
          {
            "featureType": "water",
            "elementType": "geometry",
            "stylers": [{ "color": "#101820" }]
          },
          {
            "featureType": "transit",
            "elementType": "geometry",
            "stylers": [{ "color": "#24262B" }]
          }
        ]
    """.trimIndent()

    val mapProperties = MapProperties(
        mapStyleOptions = if (isDarkMode) {
            MapStyleOptions(darkMapStyle)
        } else {
            null
        }
    )

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties
        ) {
            locations.forEach { location ->
                Marker(
                    state = MarkerState(position = location.latLng),
                    title = location.name,
                    snippet = location.description,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        getMarkerColor(location.description)
                    )
                )
            }
        }
    }
}