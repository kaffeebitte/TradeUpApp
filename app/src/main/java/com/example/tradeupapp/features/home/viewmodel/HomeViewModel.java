package com.example.tradeupapp.features.home.viewmodel;

import android.app.Application;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {
    private final MutableLiveData<Double> latitude = new MutableLiveData<>();
    private final MutableLiveData<Double> longitude = new MutableLiveData<>();
    private final MutableLiveData<String> district = new MutableLiveData<>("Bình Thạnh, Hồ Chí Minh");

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Double> getLatitude() { return latitude; }
    public LiveData<Double> getLongitude() { return longitude; }
    public LiveData<String> getDistrict() { return district; }

    public void setLocation(Double lat, Double lng) {
        latitude.setValue(lat);
        longitude.setValue(lng);
        updateDistrict(lat, lng);
    }

    public void updateDistrict(Double lat, Double lng) {
        if (lat == null || lng == null) return;
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String districtName = address.getLocality();
                    if (districtName == null) districtName = address.getSubAdminArea();
                    String city = address.getAdminArea();
                    String display = (districtName != null ? districtName : "Bình Thạnh") + ", " + (city != null ? city : "Hồ Chí Minh");
                    district.postValue(display);
                }
            } catch (IOException e) {
                // fallback to default
                district.postValue("Bình Thạnh, Hồ Chí Minh");
            }
        }).start();
    }

    public void setDefaultLocation() {
        // Bình Thạnh, Hồ Chí Minh coordinates
        setLocation(10.8021, 106.6944);
    }
}

