package com.example.ekhogo.message

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Represents one chat message
data class ChatMessage(
    val text: String,
    val isSentByMe: Boolean
)

data class ConversationPreview(
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String
)

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

    private var currentUserId: String = ""

    private var activeConversationId: String? = null
    private var listenerRegistration: ListenerRegistration? = null
    private var isMessagesScreenOpen = false
    private var latestSeenTimestamp = 0L
    private var latestMessageTimestamp = 0L
    private var hasLoadedInitialSnapshot = false

    init {
        ensureSignedInAndListen()
    }

    private fun ensureSignedInAndListen() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    currentUserId = auth.currentUser?.uid ?: ""
                    Log.d("AUTH", "Annonymous sign-in success: $currentUserId")
                }
                .addOnFailureListener { e ->
                    Log.e("AUTH", "Anonnymous sign-in failed", e)
                }
        } else {
            currentUserId = auth.currentUser?.uid ?: ""
            Log.d("AUTH", "Already signed in : $currentUserId")
        }
    }

    private fun startMessagesListener() {
        val conversationId = activeConversationId ?: return
        if (currentUserId.isBlank()) return

        listenerRegistration?.remove()
        listenerRegistration = db.collection("conversations")
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

        activeConversationId = getConversationId(currentUserId, otherUserId)
        _isInConversation.value = true
        startMessagesListener()
    }

    fun closeConversation() {
        activeConversationId = null
        _isInConversation.value = false
        _messages.value = emptyList()
    }

    fun sendMessage(text: String) {
        val conversationId = activeConversationId ?: return
        if (text.isBlank() || currentUserId.isBlank()) return

        val messageData = hashMapOf(
            "text" to text,
            "senderId" to currentUserId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(messageData)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Message sent successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "error sending message", e)
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
        listenerRegistration?.remove()
        super.onCleared()
    }

    fun loadMockConversationPreviews() {
        _conversationPreviews.value = listOf(
            ConversationPreview(
                otherUserId = "1",
                otherUserName = "Tahja Martin",
                lastMessage = "Hey, are you going to class?"
            ),
            ConversationPreview(
                otherUserId = "2",
                otherUserName = "Kristopher Arakelyan",
                lastMessage = "I sent the update last night."
            ),
            ConversationPreview(
                otherUserId = "3",
                otherUserName = "Chris Hernandez",
                lastMessage = "Did you do the PR?"
            )
        )
    }
}