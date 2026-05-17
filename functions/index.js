const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();

function stringArray(value) {
  if (!Array.isArray(value)) {
    return [];
  }

  return value.filter((item) => typeof item === "string" && item.trim() !== "");
}

function shortMessage(text) {
  if (typeof text !== "string" || text.trim() === "") {
    return "Sent you a message.";
  }

  return text.length > 120 ? `${text.slice(0, 117)}...` : text;
}

exports.sendMessageNotification = functions.firestore
  .document("notificationRequests/{requestId}")
  .onCreate(async (snapshot) => {
    const request = snapshot.data() || {};

    if (request.type !== "message") {
      await snapshot.ref.update({
        status: "skipped",
        reason: "Unsupported notification type",
        processedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      return;
    }

    const recipientIds = stringArray(request.recipientIds);
    if (recipientIds.length === 0) {
      await snapshot.ref.update({
        status: "skipped",
        reason: "No recipients",
        processedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      return;
    }

    const recipientDocs = await Promise.all(
      recipientIds.map((uid) => db.collection("users").doc(uid).get())
    );

    const tokenOwners = [];
    recipientDocs.forEach((doc, index) => {
      const uid = recipientIds[index];
      const tokens = stringArray(doc.get("fcmTokens"));

      tokens.forEach((token) => {
        tokenOwners.push({uid, token});
      });
    });

    if (tokenOwners.length === 0) {
      await snapshot.ref.update({
        status: "skipped",
        reason: "No recipient tokens",
        processedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      return;
    }

    const senderName =
      typeof request.senderName === "string" && request.senderName.trim() !== ""
        ? request.senderName
        : "Someone";
    const conversationName =
      typeof request.conversationName === "string" && request.conversationName.trim() !== ""
        ? request.conversationName
        : "Group Chat";
    const conversationId =
      typeof request.conversationId === "string" ? request.conversationId : "";

    const title = request.isGroup ? `${senderName} in ${conversationName}` : senderName;
    const body = shortMessage(request.messageText);

    const response = await admin.messaging().sendEachForMulticast({
      tokens: tokenOwners.map((owner) => owner.token),
      notification: {
        title,
        body,
      },
      data: {
        type: "message",
        conversationId,
        notificationId: snapshot.id,
        senderId: typeof request.senderId === "string" ? request.senderId : "",
        title,
        body,
      },
      android: {
        priority: "high",
        notification: {
          channelId: "messages",
          tag: snapshot.id,
        },
      },
    });

    const invalidTokens = [];
    response.responses.forEach((sendResponse, index) => {
      if (sendResponse.success) {
        return;
      }

      const errorCode = sendResponse.error && sendResponse.error.code;
      functions.logger.warn("Could not send message notification", {
        errorCode,
        uid: tokenOwners[index].uid,
      });

      if (
        errorCode === "messaging/invalid-registration-token" ||
        errorCode === "messaging/registration-token-not-registered"
      ) {
        invalidTokens.push(tokenOwners[index]);
      }
    });

    await Promise.all(
      invalidTokens.map((owner) =>
        db.collection("users").doc(owner.uid).update({
          fcmTokens: admin.firestore.FieldValue.arrayRemove(owner.token),
        })
      )
    );

    await snapshot.ref.update({
      status: "sent",
      successCount: response.successCount,
      failureCount: response.failureCount,
      invalidTokenCount: invalidTokens.length,
      processedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  });
