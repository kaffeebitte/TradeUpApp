package com.example.tradeupapp.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tradeupapp.R;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view
        initViews(view);

        // Thiết lập sự kiện tìm kiếm
        setupSearchListener();

        // Có thể hiển thị các mục tìm kiếm gần đây hoặc phổ biến
        showRecentSearches();
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.et_search);
        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler);
    }

    private void setupSearchListener() {
        // Xử lý sự kiện khi người dùng submit tìm kiếm
        searchEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch(textView.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        // TODO: Thực hiện tìm kiếm với query và hiển thị kết quả
        // searchResultsRecyclerView.setAdapter(new SearchResultAdapter(getSearchResults(query)));
    }

    private void showRecentSearches() {
        // TODO: Hiển thị các tìm kiếm gần đây hoặc phổ biến
    }
}
