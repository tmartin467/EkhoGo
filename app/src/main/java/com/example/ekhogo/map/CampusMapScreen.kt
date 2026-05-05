package com.example.ekhogo.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun CampusMapScreen(isDarkMode: Boolean) {
    val csuci = LatLng(34.1629, -119.0430)
    val context = LocalContext.current

    val categoryFilters = listOf(
        "Campus Dining",
        "Academic Building",
        "Student Housing",
        "Student Services"
    )

    var searchText by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(emptySet<String>()) }
    var showAmenityIcons by remember { mutableStateOf(true) }
    var showSuggestionDialog by remember { mutableStateOf(false) }
    val selectedAmenitySummary = remember { mutableStateOf<BuildingAmenitySummary?>(null) }
    val suggestionRepository = remember { AmenitySuggestionRepository() }
    var approvedSuggestions by remember { mutableStateOf(emptyList<BuildingAmenity>()) }
    var hasLocationPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermissions =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermissions) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(suggestionRepository) {
        val registration = suggestionRepository.listenApprovedSuggestions {
            approvedSuggestions = it
        }

        onDispose {
            registration.remove()
        }
    }

    val filteredLocations: List<CampusLocation> = remember(searchText, selectedCategories) {
        campusLocations.filter { location ->
            val matchesSearch =
                searchText.isBlank() ||
                        location.name.contains(searchText, ignoreCase = true) ||
                        location.description.contains(searchText, ignoreCase = true)

            val matchesCategory =
                selectedCategories.isEmpty() || location.description in selectedCategories

            matchesSearch && matchesCategory
        }
    }

    val buildingAmenitySummaries = remember(approvedSuggestions) {
        buildAmenitySummaries(
            buildings = campusBuildings,
            amenities = hardcodedBuildingAmenities + approvedSuggestions
        )
    }

    val visibleAmenitySummaries = remember(filteredLocations, buildingAmenitySummaries) {
        val visibleLocationNames = filteredLocations.map { it.name }.toSet()
        buildingAmenitySummaries.filter { summary ->
            summary.building.name in visibleLocationNames
        }
    }

    val displayedAmenitySummaries =
        if (showAmenityIcons) visibleAmenitySummaries else emptyList()

    val visibleSelectedAmenitySummary = selectedAmenitySummary.value?.takeIf { selectedSummary ->
        displayedAmenitySummaries.any { summary ->
            summary.building.id == selectedSummary.building.id
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
            isDarkMode = isDarkMode,
            hasLocationPermission = hasLocationPermissions,
            amenitySummaries = displayedAmenitySummaries,
            selectedAmenitySummary = visibleSelectedAmenitySummary,
            onAmenityClick = { selectedAmenitySummary.value = it },
            onAmenityDismiss = { selectedAmenitySummary.value = null },
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
            FilterChip(
                selected = selectedCategories.isEmpty(),
                onClick = {
                    selectedCategories = emptySet()
                    selectedAmenitySummary.value = null
                },
                label = { Text("All") }
            )
            categoryFilters.forEach { category ->
                FilterChip(
                    selected = category in selectedCategories,
                    onClick = {
                        selectedCategories =
                            if (category in selectedCategories) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                        selectedAmenitySummary.value = null
                    },
                    label = { Text(category) }
                )
            }
            FilterChip(
                selected = showAmenityIcons,
                onClick = {
                    showAmenityIcons = !showAmenityIcons
                    if (!showAmenityIcons) {
                        selectedAmenitySummary.value = null
                    }
                },
                label = { Text("Amenities") }
            )
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

        Button(
            onClick = { showSuggestionDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Suggest an amenity")
        }

        if (showSuggestionDialog) {
            SuggestAmenityDialog(
                repository = suggestionRepository,
                buildings = campusBuildings,
                onDismiss = { showSuggestionDialog = false }
            )
        }

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

@Composable
private fun SuggestAmenityDialog(
    repository: AmenitySuggestionRepository,
    buildings: List<CampusBuilding>,
    onDismiss: () -> Unit
) {
    var selectedBuilding by remember { mutableStateOf(buildings.first()) }
    var selectedType by remember { mutableStateOf(AmenityType.WATER_FOUNTAIN) }
    var description by remember { mutableStateOf("") }
    var buildingMenuOpen by remember { mutableStateOf(false) }
    val submitErrorMessage = remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Suggest amenity") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    OutlinedButton(onClick = { buildingMenuOpen = true }) {
                        Text(selectedBuilding.name)
                    }

                    DropdownMenu(
                        expanded = buildingMenuOpen,
                        onDismissRequest = { buildingMenuOpen = false }
                    ) {
                        buildings.forEach { building ->
                            DropdownMenuItem(
                                text = { Text(building.name) },
                                onClick = {
                                    selectedBuilding = building
                                    buildingMenuOpen = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmenityType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        submitErrorMessage.value = null
                    },
                    label = { Text("Where is it?") },
                    placeholder = { Text("Example: First floor near Room 1432") }
                )

                submitErrorMessage.value?.let { message ->
                    Text(message)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = description.isNotBlank(),
                onClick = {
                    repository.submitSuggestion(
                        building = selectedBuilding,
                        type = selectedType,
                        description = description
                    ) { success ->
                        if (success) {
                            onDismiss()
                        } else {
                            submitErrorMessage.value =
                                "Could not submit suggestion. Please try again."
                        }
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
