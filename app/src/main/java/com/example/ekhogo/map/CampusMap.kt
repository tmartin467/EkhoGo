package com.example.ekhogo.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberUpdatedMarkerState

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
    hasLocationPermission: Boolean,
    amenitySummaries: List<BuildingAmenitySummary> = emptyList(),
    onAmenityClick: (BuildingAmenitySummary) -> Unit = {},
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
        isMyLocationEnabled = hasLocationPermission,
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
            properties = mapProperties,
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = hasLocationPermission
            )
        ) {
            locations.forEach { location ->
                key(location.name, location.latLng) {
                    Marker(
                        state = rememberUpdatedMarkerState(position = location.latLng),
                        title = location.name,
                        snippet = location.description,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            getMarkerColor(location.description)
                        )
                    )
                }
            }

            amenitySummaries.forEach { summary ->
                key("amenity-${summary.building.id}") {
                    MarkerComposable(
                        summary.building.id,
                        summary.countText,
                        state = rememberUpdatedMarkerState(
                            position = amenityBadgePosition(summary.building.latLng)
                        ),
                        title = summary.building.name,
                        snippet = summary.countText,
                        zIndex = 1f,
                        onClick = {
                            onAmenityClick(summary)
                            true
                        }
                    ) {
                        AmenityCountBadge(summary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AmenityCountBadge(summary: BuildingAmenitySummary) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "W${summary.waterCount} V${summary.vendingCount}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 11.sp
            )
        }
    }
}

private fun amenityBadgePosition(position: LatLng): LatLng {
    return LatLng(
        position.latitude + 0.00005,
        position.longitude + 0.00008
    )
}
