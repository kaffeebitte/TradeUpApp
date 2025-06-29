package com.example.tradeupapp.features.listing.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.tradeupapp.models.ItemModel;

public class ItemDetailViewModel extends ViewModel {
    private final MutableLiveData<ItemModel> itemLiveData = new MutableLiveData<>();

    public void setItem(ItemModel item) {
        itemLiveData.setValue(item);
    }

    public LiveData<ItemModel> getItemLiveData() {
        return itemLiveData;
    }

    // This method would be used to fetch an item from the repository or Firestore
    public void fetchItem(String itemId) {
        // In a real implementation, this would fetch data from Firestore
        // For now we'll just handle this in the dummy data implementation

        // Example implementation with repository:
        // itemRepository.getItem(itemId).addOnSuccessListener(item -> {
        //     itemLiveData.setValue(item);
        // }).addOnFailureListener(e -> {
        //     itemLiveData.setValue(null);
        // });
    }
}
