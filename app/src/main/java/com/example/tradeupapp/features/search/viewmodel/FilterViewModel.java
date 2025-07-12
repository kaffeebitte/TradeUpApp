package com.example.tradeupapp.features.search.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FilterViewModel extends ViewModel {
    private final MutableLiveData<Double> selectedLat = new MutableLiveData<>();
    private final MutableLiveData<Double> selectedLng = new MutableLiveData<>();
    private final MutableLiveData<String> category = new MutableLiveData<>("All Categories");
    private final MutableLiveData<String> condition = new MutableLiveData<>("Any Condition");
    private final MutableLiveData<Double> minPrice = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> maxPrice = new MutableLiveData<>(Double.MAX_VALUE);
    private final MutableLiveData<Integer> distance = new MutableLiveData<>(5);
    private final MutableLiveData<String> address = new MutableLiveData<>("");

    public LiveData<Double> getSelectedLat() { return selectedLat; }
    public LiveData<Double> getSelectedLng() { return selectedLng; }
    public LiveData<String> getCategory() { return category; }
    public LiveData<String> getCondition() { return condition; }
    public LiveData<Double> getMinPrice() { return minPrice; }
    public LiveData<Double> getMaxPrice() { return maxPrice; }
    public LiveData<Integer> getDistance() { return distance; }
    public LiveData<String> getAddress() { return address; }

    public void setSelectedLat(Double lat) { selectedLat.setValue(lat); }
    public void setSelectedLng(Double lng) { selectedLng.setValue(lng); }
    public void setCategory(String cat) { category.setValue(cat); }
    public void setCondition(String cond) { condition.setValue(cond); }
    public void setMinPrice(Double min) { minPrice.setValue(min); }
    public void setMaxPrice(Double max) { maxPrice.setValue(max); }
    public void setDistance(Integer dist) { distance.setValue(dist); }
    public void setAddress(String addr) { address.setValue(addr); }

    public void reset() {
        selectedLat.setValue(null);
        selectedLng.setValue(null);
        category.setValue("All Categories");
        condition.setValue("Any Condition");
        minPrice.setValue(0.0);
        maxPrice.setValue(Double.MAX_VALUE);
        distance.setValue(5);
        address.setValue("");
    }
}

