package com.example.ekhogo.map

import com.google.android.gms.maps.model.LatLng

enum class AmenityType(val label: String) {
    WATER_FOUNTAIN("Water fountain"),
    VENDING_MACHINE("Vending machine")
}

data class CampusBuilding(
    val id: String,
    val name: String,
    val latLng: LatLng
)

data class BuildingAmenity(
    val id: String,
    val buildingId: String,
    val type: AmenityType,
    val description: String
)

data class BuildingAmenitySummary(
    val building: CampusBuilding,
    val amenities: List<BuildingAmenity>
) {
    val waterCount: Int = amenities.count { it.type == AmenityType.WATER_FOUNTAIN}
    val vendingCount: Int = amenities.count { it.type == AmenityType.VENDING_MACHINE }

    val countText: String = "W $waterCount | V $vendingCount"
}

fun buildAmenitySummaries(
    buildings: List<CampusBuilding>,
    amenities: List<BuildingAmenity>
): List<BuildingAmenitySummary> {
    val amenitiesByBuilding = amenities.groupBy { it.buildingId }

    return buildings.mapNotNull { building ->
        val buildingAmenities = amenitiesByBuilding[building.id].orEmpty()
        if (buildingAmenities.isEmpty()) null
        else BuildingAmenitySummary(building, buildingAmenities)
    }
}