package com.example.ekhogo.message

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Represents one chat message


class MessagesViewModel : ViewModel() {

    // Connect to Firebase Authentication
    private val auth = FirebaseAuth.getInstance()

    // Connects to Firebase Firestore database
    private val db = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    private val _conversationPreviews = MutableStateFlow<List<ConversationPreview>>(emptyList())

    val conversationPreviews: StateFlow<List<ConversationPreview>> =
        _conversationPreviews.asStateFlow()

    private val _isInConversation = MutableStateFlow(false)
    val isInConversation: StateFlow<Boolean> = _isInConversation.asStateFlow()
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _messageError = MutableStateFlow<String?>(null)

    private var currentUserId: String = ""

    private var activeConversationId: String? = null

    private var activeOtherUserId: String? = null
    private var messagesListenerRegistration: ListenerRegistration? = null
    private var conversationsListenerRegistration: ListenerRegistration? = null
    private var isMessagesScreenOpen = false
    private var latestSeenTimestamp = 0L
    private var latestMessageTimestamp = 0L
    private var hasLoadedInitialSnapshot = false

    init {
        refreshCurrentUserId()
    }

    private fun refreshCurrentUserId(): Boolean {
        currentUserId = auth.currentUser?.uid ?: ""
        if (currentUserId.isBlank()) {
            Log.w("AUTH", "No signed-in user available for messaging")
        }
        return currentUserId.isNotBlank()
    }

    private fun isFriendWith(otherUserId: String, onResult: (Boolean) -> Unit) {
        if (!refreshCurrentUserId()) {
            onResult(false)
            return
        }

        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val friends = (document.get("friends") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()

                onResult(friends.contains(otherUserId))
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "Error checking friendship", e)
                onResult(false)
            }
    }
    private fun startMessagesListener() {
        val conversationId = activeConversationId ?: return
        if (currentUserId.isBlank()) return

        messagesListenerRegistration?.remove()
        messagesListenerRegistration = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIREBASE", "Error loading messages", error)
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents.orEmpty()
                _messages.value = documents.map { document ->
                    val text = document.getString("text") ?: ""
                    val senderId = document.getString("senderId") ?: ""

                    ChatMessage(
                        text = text,
                        isSentByMe = senderId == currentUserId
                    )
                }

                latestMessageTimestamp = documents.maxOfOrNull { document ->
                    document.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                } ?: latestMessageTimestamp

                if (!hasLoadedInitialSnapshot) {
                    latestSeenTimestamp = latestMessageTimestamp
                    hasLoadedInitialSnapshot = true
                    _unreadCount.value = 0
                    return@addSnapshotListener
                }

                if (isMessagesScreenOpen) {
                    latestSeenTimestamp = latestMessageTimestamp
                    _unreadCount.value = 0
                    return@addSnapshotListener
                }

                _unreadCount.value = documents.count { document ->
                    val senderId = document.getString("senderId") ?: ""
                    val timestamp = document.getTimestamp("timestamp")?.toDate()?.time ?: 0L
                    senderId != currentUserId && timestamp > latestSeenTimestamp
                }
            }
    }

    // Prevents duplication of chats between the same users
    // ex: Tahja -> Chris
    // will be the same for Chris -> Tahja
    // This will be for the one-to-one messaging feature
    private fun getConversationId(currentUserId: String, otherUserId: String): String {
        return listOf(currentUserId, otherUserId)
            .sorted()
            .joinToString("_")
    }

    fun openConversation(otherUserId: String) {
        if (currentUserId.isBlank()) return

        isFriendWith(otherUserId) { isFriend ->
            if (!isFriend) {
                _messageError.value = "You can only message accepted friends."
                return@isFriendWith
            }

            activeOtherUserId = otherUserId
            activeConversationId = getConversationId(currentUserId, otherUserId)
            _isInConversation.value = true
            _messageError.value = null
            _messages.value = emptyList()
            latestSeenTimestamp = 0L
            latestMessageTimestamp = 0L
            hasLoadedInitialSnapshot = false
            startMessagesListener()
        }
    }

    fun closeConversation() {
        activeConversationId = null
        activeOtherUserId = null
        _isInConversation.value = false
        _messages.value = emptyList()
        _messageError.value = null
    }

    fun sendMessage(text: String, onComplete: (Boolean) -> Unit = {}) {
        val conversationId = activeConversationId ?: run {
            onComplete(false)
            return
        }
        val otherUserId = activeOtherUserId ?: run {
            onComplete(false)
            return
        }

        if (text.isBlank() || !refreshCurrentUserId()) {
            onComplete(false)
            return
        }

        isFriendWith(otherUserId) { isFriend ->
            if (!isFriend) {
                _messageError.value = "You can only message accepted friends."
                onComplete(false)
                return@isFriendWith
            }

            val conversationRef = db.collection("conversations").document(conversationId)
            val messageRef = conversationRef.collection("messages").document()

            val messageData = hashMapOf(
                "text" to text,
                "senderId" to currentUserId,
                "timestamp" to FieldValue.serverTimestamp()
            )

            val conversationData = hashMapOf(
                "participants" to listOf(currentUserId, otherUserId).sorted(),
                "lastMessage" to text,
                "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                "numOfParticipants" to listOf(currentUserId, otherUserId).sorted().size,
                "deletedFor" to emptyList<String>()
            )

            val batch = db.batch()
            batch.set(conversationRef, conversationData, SetOptions.merge())
            batch.set(messageRef, messageData)

            batch.commit()
                .addOnSuccessListener {
                    _messageError.value = null
                    Log.d("FIREBASE", "Message sent successfully!")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("FIREBASE", "Error sending message", e)
                    _messageError.value = "Could not send message."
                    onComplete(false)
                }
        }
    }

    fun loadConversationsPreview() {
        if (!refreshCurrentUserId()) return

        conversationsListenerRegistration?.remove()
        conversationsListenerRegistration = db.collection("conversations")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIREBASE", "Error loading conversation previews", error)
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents.orEmpty()
                    .sortedByDescending { document ->
                        document.getTimestamp("lastMessageTimestamp")?.toDate()?.time ?: 0L
                    }

                if (documents.isEmpty()) {
                    _conversationPreviews.value = emptyList()
                    return@addSnapshotListener
                }

                val previewSlots = MutableList<ConversationPreview?>(documents.size) { null }
                var remaining = documents.size

                fun publishIfReady() {
                    if (remaining == 0) {
                        _conversationPreviews.value = previewSlots.filterNotNull()
                    }
                }

                documents.forEachIndexed { index, document ->
                    val participants = (document.get("participants") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList()

                    val otherUserId = participants.firstOrNull { it != currentUserId }
                    val lastMessage = document.getString("lastMessage") ?: ""
                    val numOfParticipants = document.getLong("numOfParticipants")?.toInt() ?: 0


                    if (otherUserId == null) {
                        remaining -= 1
                        publishIfReady()
                        return@forEachIndexed
                    }

                    db.collection("users")
                        .document(otherUserId)
                        .get()
                        .addOnSuccessListener { userDocument ->
                            val otherUserName = userDocument.getString("name")
                                ?.takeIf { it.isNotBlank() }
                                ?: userDocument.getString("email")
                                ?: "Unknown User"

                            previewSlots[index] = ConversationPreview(
                                otherUserId = otherUserId,
                                otherUserName = otherUserName,
                                lastMessage = lastMessage,
                                numOfParticipants = numOfParticipants
                            )

                            remaining -= 1
                            publishIfReady()
                        }
                        .addOnFailureListener { e ->
                            Log.e("FIREBASE", "Error loading conversation user", e)

                            previewSlots[index] = ConversationPreview(
                                otherUserId = otherUserId,
                                otherUserName = "Unknown User",
                                lastMessage = lastMessage,
                                numOfParticipants = numOfParticipants
                            )

                            remaining -= 1
                            publishIfReady()
                        }
                }
            }
    }

    fun onMessagesScreenOpened() {
        isMessagesScreenOpen = true
        latestSeenTimestamp = latestMessageTimestamp
        _unreadCount.value = 0
    }

    fun onMessagesScreenClosed() {
        isMessagesScreenOpen = false
    }

    override fun onCleared() {
        messagesListenerRegistration?.remove()
        conversationsListenerRegistration?.remove()
        super.onCleared()
    }
}
