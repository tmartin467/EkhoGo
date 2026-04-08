package com.example.ekhogo.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun CampusMapScreen() {
    val csuci = LatLng(34.1629, -119.0430)

    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val filteredLocations: List<CampusLocation> = remember(searchText) {
        if (searchText.isBlank()) {
            emptyList()
        } else {
            campusLocations.filter { location: CampusLocation ->
                location.name.contains(searchText, ignoreCase = true)
            }
        }
    }

    val markersToShow: List<CampusLocation> =
        if (searchText.isBlank()) {
            emptyList()
        } else {
            filteredLocations
        }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(csuci, 16f)
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CampusMap(
            locations = markersToShow,
            cameraPositionState = cameraPositionState,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                expanded = it.isNotBlank() && filteredLocations.isNotEmpty()
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search campus locations") },
            singleLine = true
        )

        if (filteredLocations.isNotEmpty() && searchText.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                ) {
                    items(filteredLocations) { location ->
                        Text(
                            text = location.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    cameraPositionState.position =
                                        CameraPosition.fromLatLngZoom(location.latLng, 18f)
                                    searchText = location.name
                                }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}