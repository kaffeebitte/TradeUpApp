package com.example.tradeupapp.core.services;

import com.example.tradeupapp.models.ListingModel;

public interface ListingCallback {
    void onSuccess(ListingModel listing);
    void onError(String errorMessage);
}

