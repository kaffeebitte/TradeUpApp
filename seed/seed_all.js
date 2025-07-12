const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Initialize Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://your-project-id.firebaseio.com"
});

const db = admin.firestore();

// Function to load JSON data
function loadJsonData(filename) {
  const filePath = path.join(__dirname, filename);
  const rawData = fs.readFileSync(filePath);
  return JSON.parse(rawData);
}

// Function to seed a collection
async function seedCollection(collectionName, data, idField = 'id') {
  console.log(`🌱 Seeding ${collectionName}...`);

  const batch = db.batch();
  const collection = db.collection(collectionName);

  for (const item of data) {
    const docRef = collection.doc(item[idField]);
    batch.set(docRef, item);
  }

  await batch.commit();
  console.log(`✅ Successfully seeded ${data.length} documents to ${collectionName}`);
}

// Function to clear a collection
async function clearCollection(collectionName) {
  console.log(`🧹 Clearing ${collectionName}...`);

  const collection = db.collection(collectionName);
  const snapshot = await collection.get();

  if (snapshot.empty) {
    console.log(`📭 ${collectionName} is already empty`);
    return;
  }

  const batch = db.batch();
  snapshot.docs.forEach(doc => {
    batch.delete(doc.ref);
  });

  await batch.commit();
  console.log(`🗑️ Cleared ${snapshot.size} documents from ${collectionName}`);
}

// Main seeding function
async function seedAll() {
  console.log('🚀 Starting complete database seeding...\n');

  try {
    // Collections to seed in order (dependencies matter)
    const collections = [
      { name: 'categories', file: 'sample_categories.json', dataKey: 'categories' },
      { name: 'users', file: 'sample_users.json', dataKey: 'users' },
      { name: 'items', file: 'sample_items.json', dataKey: 'items' },
      { name: 'listings', file: 'sample_listings.json', dataKey: 'listings' },
      { name: 'offers', file: 'sample_offers.json', dataKey: 'offers' },
      { name: 'chats', file: 'sample_chats.json', dataKey: 'chats' },
      { name: 'chat_messages', file: 'sample_chat_messages.json', dataKey: 'messages' },
      { name: 'transactions', file: 'sample_transactions.json', dataKey: 'transactions' },
      { name: 'reviews', file: 'sample_reviews.json', dataKey: 'reviews' },
      { name: 'notifications', file: 'sample_notifications.json', dataKey: 'notifications' },
      { name: 'reports', file: 'sample_reports.json', dataKey: 'reports' },
      { name: 'admin_logs', file: 'sample_admin_logs.json', dataKey: 'adminLogs' }
    ];

    // Clear all collections first (optional)
    console.log('🧹 Clearing existing data...\n');
    for (const collection of collections) {
      await clearCollection(collection.name);
    }

    console.log('\n🌱 Starting to seed new data...\n');

    // Seed all collections
    for (const collection of collections) {
      const data = loadJsonData(collection.file);
      await seedCollection(collection.name, data[collection.dataKey]);
      console.log(''); // Add spacing between collections
    }

    // Summary
    console.log('🎉 Database seeding completed successfully!');
    console.log('\n📊 Summary:');
    console.log('- Categories: 7 items');
    console.log('- Users: 10 users');
    console.log('- Items: 25 products');
    console.log('- Listings: 25 listings');
    console.log('- Offers: 12 offers');
    console.log('- Chats: 10 conversations');
    console.log('- Chat Messages: 33 messages');
    console.log('- Transactions: 10 transactions');
    console.log('- Reviews: 12 reviews');
    console.log('- Notifications: 15 notifications');
    console.log('- Reports: 8 reports');
    console.log('- Admin Logs: 10 log entries');
    console.log('\n✨ Your TradeUp app is now ready with complete sample data!');

  } catch (error) {
    console.error('❌ Error seeding database:', error);
    process.exit(1);
  }
}

// Function to seed specific collection
async function seedSpecific(collectionName) {
  console.log(`🎯 Seeding specific collection: ${collectionName}\n`);

  const collections = {
    'categories': { file: 'sample_categories.json', dataKey: 'categories' },
    'users': { file: 'sample_users.json', dataKey: 'users' },
    'items': { file: 'sample_items.json', dataKey: 'items' },
    'listings': { file: 'sample_listings.json', dataKey: 'listings' },
    'offers': { file: 'sample_offers.json', dataKey: 'offers' },
    'chats': { file: 'sample_chats.json', dataKey: 'chats' },
    'chat_messages': { file: 'sample_chat_messages.json', dataKey: 'messages' },
    'transactions': { file: 'sample_transactions.json', dataKey: 'transactions' },
    'reviews': { file: 'sample_reviews.json', dataKey: 'reviews' },
    'notifications': { file: 'sample_notifications.json', dataKey: 'notifications' },
    'reports': { file: 'sample_reports.json', dataKey: 'reports' },
    'admin_logs': { file: 'sample_admin_logs.json', dataKey: 'adminLogs' }
  };

  if (!collections[collectionName]) {
    console.error(`❌ Unknown collection: ${collectionName}`);
    console.log('Available collections:', Object.keys(collections).join(', '));
    process.exit(1);
  }

  try {
    const collection = collections[collectionName];
    const data = loadJsonData(collection.file);

    await clearCollection(collectionName);
    await seedCollection(collectionName, data[collection.dataKey]);

    console.log(`✅ Successfully seeded ${collectionName}!`);

  } catch (error) {
    console.error(`❌ Error seeding ${collectionName}:`, error);
    process.exit(1);
  }
}

// CLI handling
const args = process.argv.slice(2);

if (args.length === 0) {
  // Seed all collections
  seedAll().then(() => {
    console.log('\n👋 Seeding complete. Goodbye!');
    process.exit(0);
  });
} else if (args[0] === '--collection' && args[1]) {
  // Seed specific collection
  seedSpecific(args[1]).then(() => {
    console.log('\n👋 Seeding complete. Goodbye!');
    process.exit(0);
  });
} else {
  console.log('📖 Usage:');
  console.log('  node seed_all.js                    # Seed all collections');
  console.log('  node seed_all.js --collection users # Seed specific collection');
  console.log('\n📚 Available collections:');
  console.log('  categories, users, items, offers, chats, chat_messages,');
  console.log('  transactions, reviews, notifications, reports, admin_logs');
  process.exit(0);
}

module.exports = {
  seedAll,
  seedSpecific,
  clearCollection,
  seedCollection
};
