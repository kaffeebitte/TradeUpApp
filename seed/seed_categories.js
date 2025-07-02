const admin = require('firebase-admin');
const fs = require('fs');

// Initialize Firebase Admin SDK
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function seedCategories() {
  try {
    console.log('Starting to seed categories...');

    // Read categories data
    const categoriesData = JSON.parse(fs.readFileSync('./sample_categories.json', 'utf8'));

    // Upload categories to Firestore
    const batch = db.batch();

    for (const category of categoriesData.categories) {
      const categoryRef = db.collection('categories').doc(category.id);
      batch.set(categoryRef, {
        name: category.name,
        description: category.description,
        icon: category.icon,
        itemCount: category.itemCount,
        isActive: category.isActive,
        sortOrder: category.sortOrder,
        subcategories: category.subcategories,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }

    await batch.commit();
    console.log(`Successfully seeded ${categoriesData.categories.length} categories`);

  } catch (error) {
    console.error('Error seeding categories:', error);
  }
}

async function updateItemCounts() {
  try {
    console.log('Updating category item counts...');

    // Get all items and count by category
    const itemsSnapshot = await db.collection('items').get();
    const categoryCounts = {};

    itemsSnapshot.forEach(doc => {
      const item = doc.data();
      const category = item.category;
      if (category) {
        categoryCounts[category] = (categoryCounts[category] || 0) + 1;
      }
    });

    // Update category documents with actual item counts
    const batch = db.batch();
    const categoriesSnapshot = await db.collection('categories').get();

    categoriesSnapshot.forEach(doc => {
      const category = doc.data();
      const categoryName = category.name;
      const itemCount = categoryCounts[categoryName] || 0;

      if (itemCount !== category.itemCount) {
        batch.update(doc.ref, {
          itemCount: itemCount,
          updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });
      }
    });

    await batch.commit();
    console.log('Category item counts updated successfully');

  } catch (error) {
    console.error('Error updating category counts:', error);
  }
}

async function main() {
  await seedCategories();
  await updateItemCounts();

  console.log('Categories seeding completed!');
  process.exit(0);
}

main();
