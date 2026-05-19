package com.example.ekhogo.map

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AmenitySuggestionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        const val REQUIRED_ADD_VOTES = 1
        const val REQUIRED_REMOVE_VOTES = 1
    }

    fun currentUserId(): String {
        return auth.currentUser?.uid.orEmpty()
    }

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
            "addVotes" to emptyList<String>(),
            "removeVotes" to emptyList<String>(),
            "addVoteCount" to 0,
            "removeVoteCount" to 0,
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

    fun listenPendingSuggestions(
        onResult: (List<AmenitySuggestion>) -> Unit,
        onError: (Exception) -> Unit = { }
    ): ListenerRegistration {
        return db.collection("amenity_suggestions")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val suggestions = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val buildingId = doc.getString("buildingId") ?: return@mapNotNull null
                    val typeName = doc.getString("type") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: return@mapNotNull null
                    val type = AmenityType.entries.firstOrNull { it.name == typeName }
                        ?: return@mapNotNull null

                    AmenitySuggestion(
                        id = doc.id,
                        buildingId = buildingId,
                        buildingName = doc.getString("buildingName") ?: buildingId,
                        type = type,
                        description = description,
                        submittedByUserId = doc.getString("submittedByUserId").orEmpty(),
                        addVoteUserIds = stringList(doc.get("addVotes")),
                        removeVoteUserIds = stringList(doc.get("removeVotes"))
                    )
                }.sortedBy { suggestion ->
                    suggestion.buildingName.lowercase()
                }

                onResult(suggestions)
            }
    }

    fun voteOnSuggestion(
        suggestionId: String,
        vote: AmenitySuggestionVote,
        onComplete: (Boolean) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onComplete(false)
            return
        }

        val suggestionRef = db.collection("amenity_suggestions").document(suggestionId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(suggestionRef)
            if (!snapshot.exists()) {
                return@runTransaction false
            }

            val status = snapshot.getString("status") ?: "pending"
            val submittedByUserId = snapshot.getString("submittedByUserId").orEmpty()
            if (status != "pending" || submittedByUserId == user.uid) {
                return@runTransaction false
            }

            val addVotes = stringList(snapshot.get("addVotes")).toMutableSet()
            val removeVotes = stringList(snapshot.get("removeVotes")).toMutableSet()

            addVotes.remove(user.uid)
            removeVotes.remove(user.uid)

            when (vote) {
                AmenitySuggestionVote.ADD -> addVotes.add(user.uid)
                AmenitySuggestionVote.REMOVE -> removeVotes.add(user.uid)
                AmenitySuggestionVote.NONE -> Unit
            }

            val nextStatus = when {
                addVotes.size >= REQUIRED_ADD_VOTES -> "approved"
                removeVotes.size >= REQUIRED_REMOVE_VOTES -> "rejected"
                else -> "pending"
            }

            val updates = mutableMapOf<String, Any>(
                "addVotes" to addVotes.toList(),
                "removeVotes" to removeVotes.toList(),
                "addVoteCount" to addVotes.size,
                "removeVoteCount" to removeVotes.size,
                "status" to nextStatus,
                "lastVotedAt" to FieldValue.serverTimestamp()
            )

            if (nextStatus == "approved") {
                updates["approvedAt"] = FieldValue.serverTimestamp()
            }

            if (nextStatus == "rejected") {
                updates["rejectedAt"] = FieldValue.serverTimestamp()
            }

            transaction.update(suggestionRef, updates)
            true
        }.addOnSuccessListener { success ->
            onComplete(success)
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    private fun stringList(value: Any?): List<String> {
        return (value as? List<*>)
            ?.filterIsInstance<String>()
            .orEmpty()
    }
}
