package com.example.ekhogo.map

import com.google.android.gms.maps.model.LatLng

val campusBuildings = listOf(
    CampusBuilding("gateway-hall", "Gateway Hall", LatLng(34.16474, -119.04545)),
    CampusBuilding("shasta-hall", "Shasta Hall", LatLng(34.16457, -119.04470)),
    CampusBuilding("napa-hall", "Napa Hall", LatLng(34.16370, -119.04565)),
    CampusBuilding("del-norte-hall", "Del Norte Hall", LatLng(34.16317, -119.04408)),
    CampusBuilding("mvs-hall", "MVS Hall", LatLng(34.16271, -119.04462)),
    CampusBuilding("sierra-hall", "Sierra Hall", LatLng(34.16227, -119.04460)),
    CampusBuilding("aliso-hall", "Aliso Hall", LatLng(34.16095, -119.04528)),
    CampusBuilding("bell-tower", "Bell Tower", LatLng(34.16097, -119.04313)),
    CampusBuilding("library", "John Spoor Broome Library", LatLng(34.16272, -119.04094))
)

val hardcodedBuildingAmenities = listOf(
    BuildingAmenity(
        id = "sierra-water-1",
        buildingId = "sierra-hall",
        type = AmenityType.WATER_FOUNTAIN,
        description =  "First floor, near Room 1432"
    ),
    BuildingAmenity(
        id = "sierra-vending-1",
        buildingId = "sierra-hall",
        type = AmenityType.VENDING_MACHINE,
        description =  "First floor, near Room 1432"
    ),
    BuildingAmenity(
        id = "sierra-vending-2",
        buildingId = "sierra-hall",
        type = AmenityType.VENDING_MACHINE,
        description =  "Second floor, near Room 2432"
    ),
    BuildingAmenity(
        id = "sierra-water-2",
        buildingId = "sierra-hall",
        type = AmenityType.WATER_FOUNTAIN,
        description =  "Second floor, near Room 2432"
    )
)