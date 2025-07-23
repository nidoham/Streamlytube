package org.schabi.newpipe.error;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.PreferenceManager;

import com.nidoham.stream.App;
import com.nidoham.stream.R;
import com.nidoham.stream.databinding.ActivityRecaptchaBinding;

import org.schabi.newpipe.DownloaderImpl;
import org.schabi.newpipe.extractor.utils.Utils;

public class ReCaptchaActivity extends AppCompatActivity {
    public static final int RECAPTCHA_REQUEST = 10;
    public static final String RECAPTCHA_URL_EXTRA = "recaptcha_url_extra";
    public static final String TAG = "ReCaptchaActivity";
    public static final String YT_URL = "https://www.youtube.com";
    public static final String RECAPTCHA_COOKIES_KEY = "recaptcha_cookies";

    public static String sanitizeRecaptchaUrl(@Nullable final String url) {
        if (url == null || url.trim().isEmpty()) {
            return YT_URL;
        } else {
            return url.replace("&pbj=1", "")
                     .replace("pbj=1&", "")
                     .replace("?pbj=1", "");
        }
    }

    private ActivityRecaptchaBinding recaptchaBinding;
    private String foundCookies = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recaptchaBinding = ActivityRecaptchaBinding.inflate(getLayoutInflater());
        setContentView(recaptchaBinding.getRoot());
        setSupportActionBar(recaptchaBinding.toolbar);

        final String url = sanitizeRecaptchaUrl(getIntent().getStringExtra(RECAPTCHA_URL_EXTRA));
        setResult(RESULT_CANCELED);

        final WebSettings webSettings = recaptchaBinding.reCaptchaWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(DownloaderImpl.USER_AGENT);
        webSettings.setDomStorageEnabled(true);

        recaptchaBinding.reCaptchaWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view,
                                                    final WebResourceRequest request) {
                if (App.DEBUG) {
                    Log.d(TAG, "shouldOverrideUrlLoading: " + request.getUrl());
                }
                handleCookiesFromUrl(request.getUrl().toString());
                return false;
            }

            @Override
            public void onPageFinished(final WebView view, final String url) {
                super.onPageFinished(view, url);
                handleCookiesFromUrl(url);
            }
        });

        clearWebViewData();
        recaptchaBinding.reCaptchaWebView.loadUrl(url);
    }

    private void clearWebViewData() {
        recaptchaBinding.reCaptchaWebView.clearCache(true);
        recaptchaBinding.reCaptchaWebView.clearHistory();
        CookieManager.getInstance().removeAllCookies(null);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recaptcha, menu);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(R.string.title_activity_recaptcha);
            actionBar.setSubtitle(R.string.subtitle_activity_recaptcha);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        saveCookiesAndFinish();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_item_done) {
            saveCookiesAndFinish();
            return true;
        }
        return false;
    }

    private void saveCookiesAndFinish() {
        handleCookiesFromUrl(recaptchaBinding.reCaptchaWebView.getUrl());
        
        if (App.DEBUG) {
            Log.d(TAG, "Saving cookies: " + foundCookies);
        }

        if (!foundCookies.isEmpty()) {
            saveCookiesToPreferences();
            updateDownloaderCookies();
            setResult(RESULT_OK);
        }

        navigateAway();
    }

    private void saveCookiesToPreferences() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(RECAPTCHA_COOKIES_KEY, foundCookies).apply();
    }

    private void updateDownloaderCookies() {
        final App app = (App) getApplication();
        if (app.isNewPipeInitialized()) {
            app.updateDownloaderCookies();
        } else {
            DownloaderImpl.getInstance().setCookie(RECAPTCHA_COOKIES_KEY, foundCookies);
        }
    }

    private void navigateAway() {
        recaptchaBinding.reCaptchaWebView.loadUrl("about:blank");
        final Intent intent = new Intent(this, com.nidoham.stream.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NavUtils.navigateUpTo(this, intent);
        finish();
    }

    private void handleCookiesFromUrl(@Nullable final String url) {
        if (App.DEBUG) {
            Log.d(TAG, "Handling cookies from URL: " + url);
        }

        if (url == null) return;

        // Get cookies from CookieManager
        final String cookies = CookieManager.getInstance().getCookie(url);
        handleCookies(cookies);

        // Handle Google abuse exemption
        handleGoogleAbuseExemption(url);
    }

    private void handleGoogleAbuseExemption(String url) {
        final int abuseStart = url.indexOf("google_abuse=");
        if (abuseStart != -1) {
            final int abuseEnd = url.indexOf("+path", abuseStart);
            if (abuseEnd != -1) {
                try {
                    final String abuseCookie = url.substring(abuseStart + 13, abuseEnd);
                    handleCookies(Utils.decodeUrlUtf8(abuseCookie));
                } catch (Exception e) {
                    if (App.DEBUG) {
                        Log.e(TAG, "Error handling abuse exemption", e);
                    }
                }
            }
        }
    }

    private void handleCookies(@Nullable final String cookies) {
        if (cookies == null) return;

        if (App.DEBUG) {
            Log.d(TAG, "Received cookies: " + cookies);
        }

        if (cookies.contains("s_gl=") || 
            cookies.contains("goojf=") ||
            cookies.contains("VISITOR_INFO1_LIVE=") ||
            cookies.contains("GOOGLE_ABUSE_EXEMPTION=")) {
            
            addCookie(cookies);
        }
    }

    private void addCookie(final String cookie) {
        if (foundCookies.contains(cookie)) return;

        if (foundCookies.isEmpty()) {
            foundCookies = cookie;
        } else {
            foundCookies += "; " + cookie;
        }

        if (App.DEBUG) {
            Log.d(TAG, "Updated cookies: " + foundCookies);
        }
    }
}