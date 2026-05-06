package com.example.ekhogo.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ekhogo.R
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

private const val AMENITY_BADGE_MIN_ZOOM = 17f

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
    modifier: Modifier = Modifier,
    locations: List<CampusLocation>,
    cameraPositionState: CameraPositionState,
    isDarkMode: Boolean,
    hasLocationPermission: Boolean,
    amenitySummaries: List<BuildingAmenitySummary> = emptyList(),
    selectedAmenitySummary: BuildingAmenitySummary? = null,
    onAmenityClick: (BuildingAmenitySummary) -> Unit = {},
    onAmenityDismiss: () -> Unit = {}
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
    val showAmenityOverlays by remember(cameraPositionState) {
        derivedStateOf {
            cameraPositionState.position.zoom >= AMENITY_BADGE_MIN_ZOOM
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

                if (showAmenityOverlays) {
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

            if (showAmenityOverlays) {
                selectedAmenitySummary?.let { summary ->
                    AmenityDetailsBox(
                        summary = summary,
                        onDismiss = onAmenityDismiss,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                    )
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
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AmenityIconCount(
                count = summary.waterCount,
                imageRes = R.drawable.amenity_water_fountain,
                contentDescription = "Water fountains"
            )
            AmenityIconCount(
                count = summary.vendingCount,
                imageRes = R.drawable.amenity_vending_machine,
                contentDescription = "Vending machines"
            )
        }
    }
}

@Composable
private fun AmenityIconCount(
    count: Int,
    imageRes: Int,
    contentDescription: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 13.sp
        )
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun AmenityDetailsBox(
    summary: BuildingAmenitySummary,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.building.name,
                    style = MaterialTheme.typography.titleSmall
                )

                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }

            Text(
                text = "${summary.waterCount} water fountains, ${summary.vendingCount} vending machines",
                style = MaterialTheme.typography.bodySmall
            )

            summary.amenities.forEach { amenity ->
                Text(
                    text = "${amenity.type.label}: ${amenity.description}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun amenityBadgePosition(position: LatLng): LatLng {
    return LatLng(
        position.latitude + 0.00005,
        position.longitude + 0.00008
    )
}
