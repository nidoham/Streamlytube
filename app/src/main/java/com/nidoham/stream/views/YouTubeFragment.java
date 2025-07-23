package com.nidoham.stream.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nidoham.stream.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Import ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nidoham.stream.adapter.VideoAdapter;
import com.nidoham.stream.databinding.FragmentsYoutubeBinding;
import com.nidoham.stream.viewmodel.YouTubeViewModel; // Import the new ViewModel

import org.schabi.newpipe.extractor.stream.StreamInfoItem;

/**
 * Fragment to display a list of trending YouTube videos.
 * Uses a ViewModel to manage data fetching and UI state, and ListAdapter for efficient RecyclerView updates.
 */
public class YouTubeFragment extends Fragment {

    private static final String TAG = "YouTubeFragment";
    private FragmentsYoutubeBinding binding;
    private YouTubeViewModel viewModel; // Declare ViewModel instance
    private VideoAdapter videoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Use ViewBinding to inflate the layout
        binding = FragmentsYoutubeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel using ViewModelProvider
        viewModel = new ViewModelProvider(this).get(YouTubeViewModel.class);

        // Setup RecyclerView with a LinearLayoutManager
        binding.rvContent.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Setup Adapter with the click listeners
        // Note: The VideoAdapter no longer takes a List in its constructor due to ListAdapter usage.
        videoAdapter = new VideoAdapter(new VideoAdapter.OnVideoClickListener() {
            @Override
            public void onVideoClick(StreamInfoItem video) {
//                Toast.makeText(getContext(), "Clicked: " + video.getName(), Toast.LENGTH_SHORT).show();
//                // TODO: Implement video detail or playback logic here
//                
//                Intent i = new Intent();
//                i.setAction(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(video.getUrl()));
//                getContext().startActivity(i);
            }

            @Override
            public void onDownloadClick(StreamInfoItem video) {
                Toast.makeText(getContext(), "Download: " + video.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Implement download handling logic here
            }
        });
        binding.rvContent.setAdapter(videoAdapter);

        // Observe LiveData from the ViewModel to update UI
        viewModel.getTrendingVideos().observe(getViewLifecycleOwner(), videos -> {
            Log.d(TAG, "Received " + videos.size() + " videos from ViewModel.");
            // Submit the new list to the ListAdapter for efficient updates
            Toast.makeText(getContext(), ""+videos.size() ,Toast.LENGTH_LONG).show();
            videoAdapter.submitList(videos);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
                // Stop swipe refresh animation if it's active
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
            }
        });

        viewModel.getIsError().observe(getViewLifecycleOwner(), isError -> {
            if (isError) {
                showError();
            } else {
                // Ensure error container is hidden if there's no error
                binding.errorContainer.setVisibility(View.GONE);
            }
        });

        // Setup Swipe Refresh Listener
        binding.swipeRefresh.setOnRefreshListener(() -> {
            // Trigger video loading via ViewModel on refresh
            viewModel.loadTrendingVideos();
        });

        // Setup Retry Button Listener
        binding.retryBtn.setOnClickListener(v -> {
            // Trigger video loading via ViewModel on retry
            viewModel.loadTrendingVideos();
        });

        // Initial load of trending videos when the fragment is created
        // The ViewModel's LiveData will automatically handle showing loading state.
        viewModel.loadTrendingVideos();
    }

    /**
     * Shows the loading indicator and hides content/error views.
     */
    private void showLoading() {
        binding.loadingContainer.setVisibility(View.VISIBLE);
        binding.rvContent.setVisibility(View.GONE);
        binding.errorContainer.setVisibility(View.GONE);
    }

    /**
     * Hides the loading indicator and shows the content RecyclerView.
     */
    private void hideLoading() {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.rvContent.setVisibility(View.VISIBLE);
        binding.errorContainer.setVisibility(View.GONE);
    }

    /**
     * Shows the error container and hides loading/content views.
     */
    private void showError() {
        binding.loadingContainer.setVisibility(View.GONE);
        binding.rvContent.setVisibility(View.GONE);
        binding.errorContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear the binding reference to prevent memory leaks
        binding = null;
    }
}