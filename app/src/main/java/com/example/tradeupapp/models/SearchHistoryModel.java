package com.example.tradeupapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model representing a user's search history entry in the TradeUpApp.
 * Contains information about previous searches performed by users.
 */
public class SearchHistoryModel implements Serializable {
    @DocumentId
    private String id;
    private String userId;
    private List<String> keywords;
    private Map<String, Object> filters;
    private Timestamp createdAt;

    /**
     * Default constructor required for Firebase Firestore
     */
    public SearchHistoryModel() {
        // Required empty constructor for Firestore
        this.keywords = new ArrayList<>();
        this.filters = new HashMap<>();
    }

    /**
     * Constructor with essential search history information
     */
    public SearchHistoryModel(String userId, List<String> keywords) {
        this.userId = userId;
        this.keywords = keywords;
        this.filters = new HashMap<>();
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor with keywords and filters
     */
    public SearchHistoryModel(String userId, List<String> keywords, Map<String, Object> filters) {
        this.userId = userId;
        this.keywords = keywords;
        this.filters = filters;
        this.createdAt = Timestamp.now();
    }

    /**
     * Full constructor with all search history properties
     */
    public SearchHistoryModel(String id, String userId, List<String> keywords,
                             Map<String, Object> filters, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.keywords = keywords;
        this.filters = filters;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Add a keyword to the search history
     * @param keyword the keyword to add
     */
    public void addKeyword(String keyword) {
        if (this.keywords == null) {
            this.keywords = new ArrayList<>();
        }
        if (!this.keywords.contains(keyword)) {
            this.keywords.add(keyword);
        }
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    /**
     * Add a filter to the search history
     * @param key filter key
     * @param value filter value
     */
    public void addFilter(String key, Object value) {
        if (this.filters == null) {
            this.filters = new HashMap<>();
        }
        this.filters.put(key, value);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the primary keyword (first in the list) if available
     * @return the primary keyword or empty string if no keywords
     */
    public String getPrimaryKeyword() {
        if (keywords != null && !keywords.isEmpty()) {
            return keywords.get(0);
        }
        return "";
    }

    /**
     * Check if this search used any filters
     * @return true if filters were used, false otherwise
     */
    public boolean hasFilters() {
        return filters != null && !filters.isEmpty();
    }

    /**
     * Get a specific filter value
     * @param key the filter key
     * @return the filter value, or null if not found
     */
    public Object getFilterValue(String key) {
        if (filters != null) {
            return filters.get(key);
        }
        return null;
    }
}
