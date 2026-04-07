package com.example.ekhogo.map

import com.google.android.gms.maps.model.LatLng

val campusLocations = listOf(
    CampusLocation(
        name = "Broome Library",
        latLng = LatLng(34.1633, -119.0412),
        description = "John Spoor Broome Library"
    ),
    CampusLocation(
        name = "Islands Cafe",
        latLng = LatLng(34.1608, -119.0413),
        description = "Campus dining"
    ),
    CampusLocation(
        name = "Lindero Hall",
        latLng = LatLng(34.1600, -119.0409),
        description = "Academic building"
    ),
    CampusLocation(
        name = "Santa Rosa Village",
        latLng = LatLng(34.1592, -119.0418),
        description = "Student housing"
    ),
    CampusLocation(
        name = "Recreation Center",
        latLng = LatLng(34.1607, -119.0446),
        description = "Fitness and recreation"
    )
)