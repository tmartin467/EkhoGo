package com.example.ekhogo.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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

    val categories = listOf(
        "All",
        "Campus Dining",
        "Academic Building",
        "Student Housing",
        "Student Services"
    )

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredLocations: List<CampusLocation> = remember(searchText, selectedCategory) {
        campusLocations.filter { location ->
            val matchesSearch =
                searchText.isBlank() ||
                        location.name.contains(searchText, ignoreCase = true) ||
                        location.description.contains(searchText, ignoreCase = true)

            val matchesCategory =
                selectedCategory == "All" || location.description == selectedCategory

            matchesSearch && matchesCategory
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(csuci, 16f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CampusMap(
            locations = filteredLocations,
            cameraPositionState = cameraPositionState,
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search campus locations") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true
        )

        if (searchText.isNotBlank() && filteredLocations.isNotEmpty()) {
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