package com.example.tradeupapp.utils;

import com.example.tradeupapp.models.ItemModel;
import com.example.tradeupapp.models.ListingModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListingFilterUtil {
    // Lọc listings theo danh sách category, logic giống personalized
    public static List<ListingModel> filterListingsByCategoriesForList(List<ListingModel> listings, List<String> categoryNames, List<ItemModel> allItems) {
        List<ListingModel> filtered = new ArrayList<>();
        Map<String, ItemModel> itemMap = new HashMap<>();
        for (ItemModel item : allItems) {
            itemMap.put(item.getId(), item);
        }
        if (categoryNames.contains("All Categories")) {
            for (ListingModel listing : listings) {
                if (listing.getTransactionStatus() != null && listing.getTransactionStatus().equalsIgnoreCase("available") && listing.isActive()) {
                    filtered.add(listing);
                }
            }
        } else {
            for (ListingModel listing : listings) {
                if (listing.getTransactionStatus() == null || !listing.getTransactionStatus().equalsIgnoreCase("available") || !listing.isActive()) continue;
                ItemModel item = itemMap.get(listing.getItemId());
                if (item != null && item.getCategory() != null) {
                    String itemCategory = item.getCategory().trim();
                    for (String selectedCat : categoryNames) {
                        if (itemCategory.equalsIgnoreCase(selectedCat)
                            || itemCategory.toLowerCase().contains(selectedCat.toLowerCase())
                            || selectedCat.toLowerCase().contains(itemCategory.toLowerCase())) {
                            filtered.add(listing);
                            break;
                        }
                    }
                }
            }
        }
        return filtered;
    }
}

