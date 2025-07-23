package com.nidoham.stream;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import org.schabi.newpipe.DownloaderImpl;
import org.schabi.newpipe.error.ReCaptchaActivity;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;

import java.util.Locale;

/**
 * Main Application class for Streamly
 * Handles NewPipe initialization, configuration, and provides global context
 */
public class App extends Application {

    // Constants
    private static final String TAG = "App";
    public static final boolean DEBUG = true;  // Enable debug features
    private static final String RECAPTCHA_COOKIES_KEY = "recaptcha_cookies_key";

    // Static instance for global access
    private static volatile App instance;
    private static volatile Context appContext;

    // Application state
    private boolean isNewPipeInitialized = false;
    private Locale currentLocale;
    private DownloaderImpl downloader;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.init(this);

        // Set static references for global access
        synchronized (App.class) {
            instance = this;
            appContext = getApplicationContext();
        }
        currentLocale = new Locale("bn", "BD"); // Default to Bangla (Bangladesh)

        // Initialize the application
        initializeApplication();

        if (DEBUG) Log.i(TAG, "Application created successfully");
    }

    /**
     * Initialize the application components
     */
    private void initializeApplication() {
        try {
            initializeNewPipe();
            isNewPipeInitialized = true;
            if (DEBUG) Log.i(TAG, "Application initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize application", e);
            isNewPipeInitialized = false;
            handleInitializationError(e);
        }
    }

    /**
     * Initialize NewPipe with custom downloader and localization settings
     */
    private void initializeNewPipe() {
        // Create localization configuration for Bangladesh
        Localization localization = new Localization("bn", "BD");
        ContentCountry contentCountry = new ContentCountry("BD");

        // Get configured downloader
        downloader = DownloaderImpl.init(null);
        setCookiesToDownloader(downloader);

        // Initialize NewPipe with custom downloader and localization
        NewPipe.init(downloader, localization, contentCountry);

        // Show Toast on main thread for successful initialization
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(appContext, "NewPipe initialized successfully", Toast.LENGTH_SHORT).show()
        );

        if (DEBUG) Log.d(TAG, "NewPipe initialized with locale: " + currentLocale);
    }

    /**
     * Set cookies to the downloader for authentication and preferences
     * @param downloader The downloader instance to configure
     */
    private void setCookiesToDownloader(final DownloaderImpl downloader) {
        try {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
            
            // Set ReCaptcha cookies
            String recaptchaCookies = prefs.getString(RECAPTCHA_COOKIES_KEY, null);
            if (recaptchaCookies != null) {
                downloader.setCookie(ReCaptchaActivity.RECAPTCHA_COOKIES_KEY, recaptchaCookies);
            }
            
            if (DEBUG) Log.d(TAG, "Cookies configured for downloader");
        } catch (Exception e) {
            Log.e(TAG, "Failed to set cookies to downloader", e);
        }
    }

    /**
     * Handle initialization errors
     * @param error The exception that occurred during initialization
     */
    private void handleInitializationError(Exception error) {
        // Show Toast on main thread for initialization failure
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(appContext, "Failed to initialize NewPipe: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        if (DEBUG) {
            Log.e(TAG, "Initialization error details: " + error.getMessage(), error);
            // Add debug-only error handling here
        }
    }

    // Global Context Methods

    /**
     * Get the singleton application instance
     * @return The App instance
     */
    public static App getInstance() {
        return instance;
    }

    /**
     * Get global application context
     * @return Application context
     */
    public static Context getAppContext() {
        return appContext;
    }

    // NewPipe Management Methods

    /**
     * Check if NewPipe is properly initialized
     * @return true if NewPipe is initialized, false otherwise
     */
    public boolean isNewPipeInitialized() {
        return isNewPipeInitialized;
    }

    /**
     * Reinitialize NewPipe (useful for error recovery)
     * @return true if reinitialization was successful, false otherwise
     */
    public boolean reinitializeNewPipe() {
        if (DEBUG) Log.d(TAG, "Attempting to reinitialize NewPipe");
        try {
            initializeNewPipe();
            isNewPipeInitialized = true;
            if (DEBUG) Log.i(TAG, "NewPipe reinitialized successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to reinitialize NewPipe", e);
            isNewPipeInitialized = false;
            return false;
        }
    }

    /**
     * Update downloader cookies (useful for authentication changes)
     */
    public void updateDownloaderCookies() {
        if (downloader != null) {
            setCookiesToDownloader(downloader);
            if (DEBUG) Log.d(TAG, "Downloader cookies updated");
        }
    }

    // Utility Methods

    /**
     * Get the current locale being used by the application
     * @return Current locale
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Get shared preferences instance
     * @return SharedPreferences instance
     */
    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (DEBUG) Log.w(TAG, "Low memory warning received");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (DEBUG) Log.i(TAG, "Application terminated");
        // Clear static references
        synchronized (App.class) {
            instance = null;
            appContext = null;
        }
    }
    
    // ================= DEBUG-ONLY METHODS ================= //
    /**
     * DEBUG ONLY: Get downloader instance for inspection
     */
    public DownloaderImpl getDownloaderForDebug() {
        if (!DEBUG) {
            throw new IllegalStateException("Debug method called in release mode");
        }
        return downloader;
    }
    
    /**
     * DEBUG ONLY: Simulate initialization failure
     */
    public void debugSimulateInitFailure() {
        if (!DEBUG) return;
        isNewPipeInitialized = false;
        downloader = null;
        Log.w(TAG, "DEBUG: Simulated initialization failure");
    }
}