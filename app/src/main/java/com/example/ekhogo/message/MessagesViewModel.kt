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

    private val _selectedOtherUser = MutableStateFlow("")

    val selectedOtherUser = _selectedOtherUser.asStateFlow()

    private val _isGroupChat = MutableStateFlow(false)

    val isGroupChat = _isGroupChat.asStateFlow()


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
                    val id = document.id
                    ChatMessage(
                        text = text,
                        isSentByMe = senderId == currentUserId,
                        id = id,
                        senderId = senderId
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

    fun selectOtherUser(name: String) {
        _selectedOtherUser.value = name
    }

    fun openConversation(conversationId: String, isGroup: Boolean) {
        if (currentUserId.isBlank()) return
        activeConversationId = conversationId
        _isGroupChat.value = isGroup
        _isInConversation.value = true
        _messageError.value = null
        _messages.value = emptyList()
        latestSeenTimestamp = 0L
        latestMessageTimestamp = 0L
        hasLoadedInitialSnapshot = false
        startMessagesListener()

    }

    fun createGroupConversation(
        groupName: String,
        selectedUserIds: List<String>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (currentUserId.isBlank()) {
            onComplete(false)
            return
        }

        val participants = (selectedUserIds + currentUserId).distinct()

        val conversationRef = db.collection("conversations").document()

        val conversationData = hashMapOf(
            "groupName" to groupName,
            "participants" to participants,
            "lastMessage" to "",
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "numOfParticipants" to participants.size,
            "isGroup" to true,
            "deletedFor" to emptyList<String>()
        )

        conversationRef.set(conversationData)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun closeConversation() {
        activeConversationId = null
        _isInConversation.value = false
        _messages.value = emptyList()
        _messageError.value = null
    }

    fun sendMessage(text: String, onComplete: (Boolean) -> Unit = {}) {
        val conversationId = activeConversationId ?: run {
            onComplete(false)
            return
        }


        if (text.isBlank() || !refreshCurrentUserId()) {
            onComplete(false)
            return
        }


        val conversationRef = db.collection("conversations").document(conversationId)
        val messageRef = conversationRef.collection("messages").document()

        val messageData = hashMapOf(
            "text" to text,
            "senderId" to currentUserId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        val conversationData = hashMapOf(
            "lastMessage" to text,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
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
        //}
    }

    fun loadConversationsPreview() {
        if (!refreshCurrentUserId()) return

        conversationsListenerRegistration?.remove()
        conversationsListenerRegistration =
            db.collection("conversations")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        Log.e("FIREBASE", "Error loading conversation previews", error)
                        return@addSnapshotListener
                    }

                    val documents = snapshot?.documents.orEmpty()
                        .sortedByDescending {
                            it.getTimestamp("lastMessageTimestamp")?.toDate()?.time ?: 0L
                        }

                    if (documents.isEmpty()) {
                        _conversationPreviews.value = emptyList()
                        return@addSnapshotListener
                    }

                    val previews = mutableListOf<ConversationPreview>()
                    var count = documents.size

                    fun publish() {
                        if (count == 0) {
                            _conversationPreviews.value = previews.toList()
                        }
                    }

                    documents.forEach { document ->

                        val participants = (document.get("participants") as? List<*>)
                            ?.filterIsInstance<String>()
                            ?: emptyList()

                        val isGroup = document.getBoolean("isGroup") ?: false
                        val lastMessage = document.getString("lastMessage") ?: ""
                        val deletedFor = document.get("deletedFor") as? List<String> ?: emptyList()

                        if (isGroup) {
                            val groupName = document.getString("groupName") ?: "Group Chat"

                            previews.add(
                                ConversationPreview(
                                    conversationId = document.id,
                                    otherUserId = "",
                                    otherUserName = groupName,
                                    lastMessage = lastMessage,
                                    numOfParticipants = participants.size,
                                    deletedFor = deletedFor,
                                    isGroup = true
                                )
                            )

                            count--
                            publish()
                            return@forEach
                        }

                        val otherUserId = participants.firstOrNull { it != currentUserId }

                        if (otherUserId == null) {
                            count--
                            publish()
                            return@forEach
                        }

                        db.collection("users")
                            .document(otherUserId)
                            .get()
                            .addOnSuccessListener { userDoc ->

                                val otherUserName =
                                    userDoc.getString("name")
                                        ?.takeIf { it.isNotBlank() }
                                        ?: userDoc.getString("email")
                                        ?: "Unknown User"

                                previews.add(
                                    ConversationPreview(
                                        conversationId = document.id,
                                        otherUserId = otherUserId,
                                        otherUserName = otherUserName,
                                        lastMessage = lastMessage,
                                        numOfParticipants = participants.size,
                                        deletedFor = deletedFor,
                                        isGroup = false
                                    )
                                )

                                count--
                                publish()
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


    fun startDirectConversation(otherUserId: String, onComplete: (String) -> Unit) {
        if (!refreshCurrentUserId()) return

        val conversationId = getConversationId(currentUserId, otherUserId)

        val ref = db.collection("conversations").document(conversationId)

        ref.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                val data = hashMapOf(
                    "participants" to listOf(currentUserId, otherUserId),
                    "lastMessage" to "",
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "isGroup" to false,
                    "deletedFor" to emptyList<String>()
                )

                ref.set(data)
            }

            onComplete(conversationId)
        }
    }

        fun unsendMessage(message: String) {

            val convoID = activeConversationId ?: return
            val message: String = message

            FirebaseFirestore.getInstance()
                .collection("conversations")
                .document(convoID)
                .collection("messages")
                .document(message)
                .delete()

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")

                    sendMessage("${name} unsent a message")
                }

        }

        override fun onCleared() {
            messagesListenerRegistration?.remove()
            conversationsListenerRegistration?.remove()
            super.onCleared()
        }
    }
