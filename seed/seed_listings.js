const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Initialize Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'tradeup-app-46648'
});

const db = admin.firestore();

async function seedListings() {
  try {
    console.log('🌱 Starting to seed listings data...');
    const dataPath = path.join(__dirname, 'sample_listings.json');
    const rawData = fs.readFileSync(dataPath, 'utf8');
    const data = JSON.parse(rawData);

    const listings = data.listings;
    console.log(`📦 Found ${listings.length} listings to seed`);

    // Optional: Clear existing listings
    const snapshot = await db.collection('listings').get();
    const batchDelete = db.batch();
    snapshot.docs.forEach(doc => batchDelete.delete(doc.ref));
    await batchDelete.commit();

    // Batch write for new listings
    const batch = db.batch();
    listings.forEach(listing => {
      if (listing.createdAt) {
        listing.createdAt = admin.firestore.Timestamp.fromDate(new Date(listing.createdAt));
      }
      if (listing.updatedAt) {
        listing.updatedAt = admin.firestore.Timestamp.fromDate(new Date(listing.updatedAt));
      }
      const docRef = db.collection('listings').doc(listing.id);
      batch.set(docRef, listing);
    });
    await batch.commit();

    console.log('✅ Successfully seeded all listings!');
    console.log(`📊 Total listings added: ${listings.length}`);
    process.exit(0);
  } catch (error) {
    console.error('❌ Error seeding listings:', error);
    process.exit(1);
  }
}

seedListings();
