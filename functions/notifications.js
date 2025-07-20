const functions = require('firebase-functions');
const admin = require('firebase-admin');
if (!admin.apps.length) admin.initializeApp();
const db = admin.firestore();

// Helper: create notification
async function createNotification({ userId, title, body, type, relatedId }) {
  await db.collection('notifications').add({
    userId,
    title,
    body,
    type,
    read: false,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    relatedId,
  });
}

// Helper: get buyer name
async function getUserName(userId) {
  const userSnap = await db.collection('users').doc(userId).get();
  const user = userSnap.data();
  return user?.displayName || user?.name || userId;
}

// Helper: get item title
async function getItemTitle(itemId) {
  const itemSnap = await db.collection('items').doc(itemId).get();
  const item = itemSnap.data();
  return item?.title || 'your item';
}

// Trigger: on new offer (push notification to seller)
exports.onOfferCreated = functions.firestore
  .document('offers/{offerId}')
  .onCreate(async (snap, context) => {
    const offer = snap.data();
    if (!offer) return;
    const listingSnap = await db.collection('listings').doc(offer.listingId).get();
    const listing = listingSnap.data();
    if (!listing) return;
    const itemTitle = await getItemTitle(listing.itemId);
    const buyerName = await getUserName(offer.buyerId);
    const sellerId = offer.sellerId;
    // Notify seller
    await createNotification({
      userId: sellerId,
      title: `New offer for ${itemTitle}`,
      body: `${buyerName} offered ${offer.offerAmount.toLocaleString()} VND`,
      type: 'offer',
      relatedId: offer.id || context.params.offerId,
    });
  });

// Trigger: on listing update (push notification to users who saved listing)
exports.onListingUpdated = functions.firestore
  .document('listings/{listingId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    if (!before || !after) return;
    // Only notify if price or active status changed
    if (before.price === after.price && before.isActive === after.isActive) return;
    const itemTitle = await getItemTitle(after.itemId);
    // Find users who saved this listing
    const userInteractions = after.interactions?.userInteractions || [];
    for (const entry of userInteractions) {
      if (entry.savedAt) {
        await createNotification({
          userId: entry.userId,
          title: `Listing updated: ${itemTitle}`,
          body: `A listing you saved has been updated. Check the new details!`,
          type: 'listing_update',
          relatedId: after.id || context.params.listingId,
        });
      }
    }
  });
