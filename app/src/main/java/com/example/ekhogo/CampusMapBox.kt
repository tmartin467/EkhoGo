package com.example.ekhogo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

@Composable
fun CampusMapBox() {

        val csuci = LatLng(34.1629, -119.0430)

        val cameraPositonState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(csuci, 16f)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .aspectRatio(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositonState
            ) {
                Marker(
                    state = MarkerState(position = csuci),
                    title = "CSUCI",
                    snippet = "EkhoGo Map Test"
                )
            }
        }
}