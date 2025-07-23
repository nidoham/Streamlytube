package org.schabi.newpipe;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.nidoham.stream.App;
import com.nidoham.stream.R;
import org.schabi.newpipe.error.ReCaptchaActivity;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.util.InfoCache;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public final class DownloaderImpl extends Downloader {
    public static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0";
    public static final String YOUTUBE_RESTRICTED_MODE_COOKIE_KEY = "youtube_restricted_mode_key";
    public static final String YOUTUBE_RESTRICTED_MODE_COOKIE = "PREF=f2=8000000";
    public static final String YOUTUBE_DOMAIN = "youtube.com";
    private static final String TAG = "DownloaderImpl";

    private static volatile DownloaderImpl instance;
    private final Map<String, String> mCookies;
    private final OkHttpClient client;

    private DownloaderImpl(final OkHttpClient.Builder builder) {
        this.client = builder
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .build();
        this.mCookies = new HashMap<>();
    }

    public static synchronized DownloaderImpl init(@Nullable final OkHttpClient.Builder builder) {
        if (instance == null) {
            instance = new DownloaderImpl(
                builder != null ? builder : new OkHttpClient.Builder()
            );
            if (App.DEBUG) {
                android.util.Log.d(TAG, "DownloaderImpl initialized");
            }
        }
        return instance;
    }

    public static synchronized DownloaderImpl getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DownloaderImpl not initialized. Call init() first.");
        }
        return instance;
    }

    public String getCookies(final String url) {
        final StringBuilder cookiesBuilder = new StringBuilder();
        
        // Add YouTube restricted mode cookie if applicable
        if (url.contains(YOUTUBE_DOMAIN)) {
            final String youtubeCookie = getCookie(YOUTUBE_RESTRICTED_MODE_COOKIE_KEY);
            if (youtubeCookie != null) {
                cookiesBuilder.append(youtubeCookie).append("; ");
            }
        }
        
        // Add ReCaptcha cookies
        final String recaptchaCookie = getCookie(ReCaptchaActivity.RECAPTCHA_COOKIES_KEY);
        if (recaptchaCookie != null) {
            cookiesBuilder.append(recaptchaCookie);
        }
        
        return cookiesBuilder.toString().trim();
    }

    public String getCookie(final String key) {
        return mCookies.get(key);
    }

    public void setCookie(final String key, final String cookie) {
        if (cookie == null) {
            mCookies.remove(key);
        } else {
            mCookies.put(key, cookie);
            if (App.DEBUG) {
                android.util.Log.d(TAG, "Set cookie for key: " + key);
            }
        }
    }

    public void removeCookie(final String key) {
        mCookies.remove(key);
        if (App.DEBUG) {
            android.util.Log.d(TAG, "Removed cookie for key: " + key);
        }
    }

    public void updateYoutubeRestrictedModeCookies() {
        final String restrictedModeEnabledKey = App.getAppContext().getString(R.string.youtube_restricted_mode_enabled);
        final boolean restrictedModeEnabled = PreferenceManager.getDefaultSharedPreferences(App.getAppContext())
                .getBoolean(restrictedModeEnabledKey, false);
        updateYoutubeRestrictedModeCookies(restrictedModeEnabled);
    }

    public void updateYoutubeRestrictedModeCookies(final boolean youtubeRestrictedModeEnabled) {
        if (youtubeRestrictedModeEnabled) {
            setCookie(YOUTUBE_RESTRICTED_MODE_COOKIE_KEY, YOUTUBE_RESTRICTED_MODE_COOKIE);
        } else {
            removeCookie(YOUTUBE_RESTRICTED_MODE_COOKIE_KEY);
        }
        InfoCache.getInstance().clearCache();
        // Show Toast for cookie update
        new Handler(Looper.getMainLooper()).post(() ->
            Toast.makeText(App.getAppContext(),
                youtubeRestrictedModeEnabled ? "YouTube restricted mode enabled" : "YouTube restricted mode disabled",
                Toast.LENGTH_SHORT).show()
        );
        if (App.DEBUG) {
            android.util.Log.d(TAG, "Updated YouTube restricted mode: " + youtubeRestrictedModeEnabled);
        }
    }

    public long getContentLength(final String url) throws IOException {
        try {
            final Response response = head(url);
            final String contentLength = response.getHeader("Content-Length");
            if (contentLength != null && !contentLength.isEmpty()) {
                return Long.parseLong(contentLength);
            } else {
                throw new IOException("Content-Length header missing");
            }
        } catch (NumberFormatException | ReCaptchaException e) {
            throw new IOException("Error getting content length", e);
        }
    }

    @Override
    public Response execute(@NonNull final Request request) throws IOException, ReCaptchaException {
        final String httpMethod = request.httpMethod();
        final String url = request.url();
        final Map<String, List<String>> headers = request.headers();
        final byte[] dataToSend = request.dataToSend();

        RequestBody requestBody = null;
        if (dataToSend != null && dataToSend.length > 0) {
            requestBody = RequestBody.create(dataToSend, null);
        }

        final okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                .url(url)
                .method(httpMethod, requestBody)
                .addHeader("User-Agent", USER_AGENT);

        // Add cookies if available
        final String cookies = getCookies(url);
        if (!cookies.isEmpty()) {
            requestBuilder.addHeader("Cookie", cookies);
        }

        // Add custom headers
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            final String headerName = header.getKey();
            requestBuilder.removeHeader(headerName);
            for (String headerValue : header.getValue()) {
                requestBuilder.addHeader(headerName, headerValue);
            }
        }

        try {
            final okhttp3.Response response = client.newCall(requestBuilder.build()).execute();
            return handleResponse(response);
        } catch (IOException e) {
            throw new IOException("Network error: " + e.getMessage(), e);
        }
    }

    private Response handleResponse(okhttp3.Response response) throws IOException, ReCaptchaException {
        final int code = response.code();
        final String message = response.message();
        final Map<String, List<String>> headers = convertHeaders(response.headers());
        final String responseBody = getResponseBody(response);
        final String finalUrl = response.request().url().toString();

        if (code == 429) {
            response.close();
            // Show Toast for ReCaptcha challenge
            new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(App.getAppContext(),
                    "ReCaptcha challenge required",
                    Toast.LENGTH_LONG).show()
            );
            throw new ReCaptchaException("reCaptcha Challenge requested", finalUrl);
        }

        return new Response(code, message, headers, responseBody, finalUrl);
    }

    private Map<String, List<String>> convertHeaders(Headers headers) {
        final Map<String, List<String>> result = new HashMap<>();
        for (String name : headers.names()) {
            result.put(name, headers.values(name));
        }
        return Collections.unmodifiableMap(result);
    }

    private String getResponseBody(okhttp3.Response response) throws IOException {
        try (ResponseBody body = response.body()) {
            return body != null ? body.string() : "";
        }
    }
}