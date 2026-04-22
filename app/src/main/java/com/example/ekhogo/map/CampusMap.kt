package com.example.ekhogo.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
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