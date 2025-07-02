const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Initialize Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json'); // You need to download this from Firebase Console

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'tradeup-app-46648' // Your actual project ID
});

const db = admin.firestore();

/**
 * Seeds sample items data into Firestore
 */
async function seedItems() {
  try {
    console.log('🌱 Starting to seed items data...');

    // Read the sample data
    const dataPath = path.join(__dirname, 'sample_items.json');
    const rawData = fs.readFileSync(dataPath, 'utf8');
    const data = JSON.parse(rawData);

    const items = data.items;
    console.log(`📦 Found ${items.length} items to seed`);

    // Clear existing items (optional)
    await clearExistingItems();

    // Batch write for better performance
    const batch = db.batch();

    items.forEach(item => {
      // Convert dateAdded string to Firestore Timestamp
      if (item.dateAdded) {
        item.dateAdded = admin.firestore.Timestamp.fromDate(new Date(item.dateAdded));
      }

      const docRef = db.collection('items').doc(item.id);
      batch.set(docRef, item);
    });

    // Commit the batch
    await batch.commit();

    console.log('✅ Successfully seeded all items!');
    console.log(`📊 Total items added: ${items.length}`);

    // Print some statistics
    printStatistics(items);

  } catch (error) {
    console.error('❌ Error seeding items:', error);
    process.exit(1);
  }
}

/**
 * Clears existing items from Firestore
 */
async function clearExistingItems() {
  console.log('🗑️  Clearing existing items...');

  const snapshot = await db.collection('items').get();

  if (snapshot.empty) {
    console.log('📭 No existing items to clear');
    return;
  }

  const batch = db.batch();
  snapshot.docs.forEach(doc => {
    batch.delete(doc.ref);
  });

  await batch.commit();
  console.log(`🗑️  Cleared ${snapshot.size} existing items`);
}

/**
 * Prints statistics about the seeded data
 */
function printStatistics(items) {
  console.log('\n📈 Data Statistics:');

  // Categories
  const categories = {};
  items.forEach(item => {
    categories[item.category] = (categories[item.category] || 0) + 1;
  });

  console.log('\n📂 Categories:');
  Object.entries(categories)
    .sort(([,a], [,b]) => b - a)
    .forEach(([category, count]) => {
      console.log(`  ${category}: ${count} items`);
    });

  // Price ranges
  const prices = items.map(item => item.price).sort((a, b) => a - b);
  console.log('\n💰 Price Range:');
  console.log(`  Lowest: ${formatPrice(prices[0])}`);
  console.log(`  Highest: ${formatPrice(prices[prices.length - 1])}`);
  console.log(`  Average: ${formatPrice(prices.reduce((a, b) => a + b, 0) / prices.length)}`);

  // Conditions
  const conditions = {};
  items.forEach(item => {
    conditions[item.condition] = (conditions[item.condition] || 0) + 1;
  });

  console.log('\n🔍 Conditions:');
  Object.entries(conditions).forEach(([condition, count]) => {
    console.log(`  ${condition}: ${count} items`);
  });

  // Tags
  const tags = {};
  items.forEach(item => {
    if (item.tag) {
      tags[item.tag] = (tags[item.tag] || 0) + 1;
    }
  });

  console.log('\n🏷️  Tags:');
  Object.entries(tags)
    .sort(([,a], [,b]) => b - a)
    .forEach(([tag, count]) => {
      console.log(`  ${tag}: ${count} items`);
    });
}

/**
 * Formats price in VND
 */
function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price);
}

/**
 * Seeds sample users data (basic user profiles for the items)
 */
async function seedUsers() {
  console.log('👥 Seeding sample users...');

  const users = [];
  for (let i = 1; i <= 20; i++) {
    const userId = `user_${i.toString().padStart(3, '0')}`;
    users.push({
      uid: userId,
      displayName: `User ${i}`,
      email: `user${i}@example.com`,
      photoUrl: `https://example.com/avatar_${i}.jpg`,
      bio: `Sample user ${i} for testing purposes`,
      role: 'user',
      rating: Math.random() * 5,
      totalReviews: Math.floor(Math.random() * 50),
      createdAt: admin.firestore.Timestamp.now(),
      isActive: true,
      isDeleted: false,
      location: null
    });
  }

  const batch = db.batch();
  users.forEach(user => {
    const docRef = db.collection('users').doc(user.uid);
    batch.set(docRef, user);
  });

  await batch.commit();
  console.log(`✅ Seeded ${users.length} sample users`);
}

/**
 * Main function to run all seeding operations
 */
async function main() {
  console.log('🚀 Starting TradeUpApp data seeding...\n');

  try {
    await seedUsers();
    await seedItems();

    console.log('\n🎉 All data seeding completed successfully!');
    console.log('🔗 You can now view your data in Firebase Console');

  } catch (error) {
    console.error('💥 Failed to seed data:', error);
  } finally {
    // Close the connection
    process.exit(0);
  }
}

// Command line interface
const args = process.argv.slice(2);
const command = args[0];

switch (command) {
  case 'items':
    seedItems();
    break;
  case 'users':
    seedUsers();
    break;
  case 'clear':
    clearExistingItems().then(() => {
      console.log('✅ Cleared all items');
      process.exit(0);
    });
    break;
  default:
    main();
    break;
}

module.exports = {
  seedItems,
  seedUsers,
  clearExistingItems
};
