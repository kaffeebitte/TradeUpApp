/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const logger = require("firebase-functions/logger");
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp({
  credential: admin.credential.applicationDefault(),
});

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
/**
 * Send push notification to user when a new chat message is received
 * Expects POST with: { recipientUserId, message, senderName }
 * Looks up fcmToken from Firestore automatically
 */
exports.sendMessageNotification = functions.https.onRequest(async (req, res) => {
  if (req.method !== 'POST') {
    return res.status(405).send('Method Not Allowed');
  }
  const { recipientUserId, message, senderName } = req.body;
  if (!recipientUserId || !message || !senderName) {
    return res.status(400).send('Missing required fields');
  }
  try {
    // Fetch fcmToken from Firestore
    const userDoc = await admin.firestore().collection('users').doc(recipientUserId).get();
    if (!userDoc.exists) {
      return res.status(404).send('Recipient user not found');
    }
    const fcmToken = userDoc.get('fcmToken');
    if (!fcmToken) {
      return res.status(400).send('Recipient does not have a valid fcmToken');
    }
    const payload = {
      notification: {
        title: `New message from ${senderName}`,
        body: message,
      },
      data: {
        type: 'chat_message',
        senderName,
      },
    };
    await admin.messaging().sendToDevice(fcmToken, payload);
    return res.status(200).send('Notification sent');
  } catch (error) {
    logger.error('FCM error', error);
    return res.status(500).send('Failed to send notification');
  }
});
