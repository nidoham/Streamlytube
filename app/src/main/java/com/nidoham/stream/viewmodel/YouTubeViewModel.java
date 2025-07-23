package com.nidoham.stream.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for YouTubeFragment to handle data fetching and UI state.
 * It encapsulates the logic for loading trending videos and exposes data
 * and loading/error states via LiveData.
 */
public class YouTubeViewModel extends AndroidViewModel {

    private static final String TAG = "YouTubeViewModel";

    // LiveData for the list of trending videos
    private final MutableLiveData<List<StreamInfoItem>> _trendingVideos = new MutableLiveData<>();
    public LiveData<List<StreamInfoItem>> getTrendingVideos() {
        return _trendingVideos;
    }

    // LiveData for the loading state
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    // LiveData for the error state
    private final MutableLiveData<Boolean> _isError = new MutableLiveData<>();
    public LiveData<Boolean> getIsError() {
        return _isError;
    }

    private final ExecutorService executorService;

    public YouTubeViewModel(@NonNull Application application) {
        super(application);
        // Initialize ExecutorService for background operations
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Initiates the loading of trending videos from YouTube.
     * Updates LiveData for loading, error, and video list states.
     */
    public void loadTrendingVideos() {
        _isLoading.postValue(true); // Indicate loading has started
        _isError.postValue(false);  // Reset error state

        executorService.execute(() -> {
            try {
                Log.d(TAG, "Loading trending videos...");
                // Create KioskExtractor for YouTube Trending
                KioskExtractor extractor = ServiceList.YouTube
                        .getKioskList()
                        .getExtractorById("Trending", null, Localization.DEFAULT);
                extractor.fetchPage();
                List<StreamInfoItem> items = extractor.getInitialPage().getItems();
                Log.d(TAG, "Total videos found: " + items.size());

                _trendingVideos.postValue(items); // Update LiveData with fetched videos
                _isLoading.postValue(false);     // Indicate loading has finished
            } catch (ExtractionException | IOException e) {
                Log.e(TAG, "Failed to load videos", e);
                _isError.postValue(true);        // Indicate an error occurred
                _isLoading.postValue(false);     // Indicate loading has finished (even with error)
            }
        });
    }

    /**
     * Called when the ViewModel is no longer used and will be destroyed.
     * Shuts down the ExecutorService to prevent memory leaks.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow(); // Shut down immediately
        }
    }
}