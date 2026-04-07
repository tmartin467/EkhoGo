package com.example.ekhogo.map

import com.google.android.gms.maps.model.LatLng

data class CampusLocation(
    val name: String,
    val latLng: LatLng,
    val description: String = ""
)