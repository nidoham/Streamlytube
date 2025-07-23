package com.nidoham.stream.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
//import com.nidoham.stream.databinding.FragmentSearchBinding;

public class SearchFragment extends Fragment {
    
  /*  private FragmentSearchBinding binding;
    private SearchAdapter searchAdapter;
    private List<SearchItem> searchResults; */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //binding = FragmentSearchBinding.inflate(inflater, container, false);
        
       /* setupRecyclerView();
        setupSearchButton(); */
        
        return null; //binding.getRoot();
    }
    
   /* private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        searchAdapter = new SearchAdapter(searchResults);
        binding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchRecyclerView.setAdapter(searchAdapter);
        
        // Load sample data
        loadSampleData();
    }
    
    private void setupSearchButton() {
        binding.searchButton.setOnClickListener(v -> {
            String query = binding.searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });
    }
    
    private void performSearch(String query) {
        // Implement search logic here
        // For now, just filter existing results
        searchResults.clear();
        loadSampleData(); // Replace with actual search implementation
        searchAdapter.notifyDataSetChanged();
    }
    
    private void loadSampleData() {
        searchResults.add(new SearchItem("Sample Video 1", "Description 1", "https://example.com/thumb1.jpg"));
        searchResults.add(new SearchItem("Sample Video 2", "Description 2", "https://example.com/thumb2.jpg"));
        searchResults.add(new SearchItem("Sample Video 3", "Description 3", "https://example.com/thumb3.jpg"));
        searchResults.add(new SearchItem("Sample Music 1", "Artist - Album", "https://example.com/music1.jpg"));
        searchResults.add(new SearchItem("Sample Music 2", "Artist - Album", "https://example.com/music2.jpg"));
    } */
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
       // binding = null;
    }
}