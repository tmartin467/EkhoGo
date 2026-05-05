package com.example.ekhogo.map

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AmenitySuggestionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun submitSuggestion(
        building: CampusBuilding,
        type: AmenityType,
        description: String,
        onComplete: (Boolean) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onComplete(false)
            return
        }

        val suggestion = hashMapOf(
            "buildingId" to building.id,
            "buildingName" to building.name,
            "type" to type.name,
            "description" to description.trim(),
            "status" to "pending",
            "submittedByUserId" to user.uid,
            "submittedAt" to FieldValue.serverTimestamp()
        )

        db.collection("amenity_suggestions")
            .add(suggestion)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun listenApprovedSuggestions(
        onResult: (List<BuildingAmenity>) -> Unit,
        onError: (Exception) -> Unit = { }
    ): ListenerRegistration {
        return db.collection("amenity_suggestions")
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val amenities = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val buildingId = doc.getString("buildingId") ?: return@mapNotNull null
                    val typeName = doc.getString("type") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: return@mapNotNull null
                    val type = AmenityType.entries.firstOrNull { it.name == typeName }
                        ?: return@mapNotNull null

                    BuildingAmenity(
                        id = doc.id,
                        buildingId = buildingId,
                        type = type,
                        description = description
                    )
                }

                onResult(amenities)
            }
    }
}