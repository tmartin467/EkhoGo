const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const {getMessaging} = require("firebase-admin/messaging");
const {onDocumentCreated} = require("firebase-functions/v2/firestore");

initializeApp();

const db = getFirestore();

exports.sendMessageNotification = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
  async (event) => {
    const message = event.data && event.data.data();
    if (!message) return;

    const senderId = message.senderId;
    const text = message.text || "New message";
    if (!senderId) return;

    const conversation = await db
      .collection("conversations")
      .doc(event.params.conversationId)
      .get();

    const participants = conversation.get("participants") || [];
    const recipientIds = participants.filter((uid) => uid !== senderId);
    if (recipientIds.length === 0) return;

    const sender = await db.collection("users").doc(senderId).get();
    const senderName = sender.get("name") || sender.get("email") || "Someone";

    const tokenSnapshots = await Promise.all(
      recipientIds.map((uid) => {
        return db.collection("users").doc(uid).collection("fcmTokens").get();
      }),
    );

    const tokens = tokenSnapshots.flatMap((snapshot) => {
      return snapshot.docs.map((doc) => doc.id);
    });

    if (tokens.length === 0) return;

    await getMessaging().sendEachForMulticast({
      tokens,
      data: {
        type: "message",
        senderName,
        body: text,
      },
      android: {
        priority: "high",
      },
    });
  },
);
