package org.schabi.newpipe.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.nidoham.stream.App;
import org.schabi.newpipe.extractor.Info;

import java.util.Map;

public final class InfoCache {
    private final String TAG = "InfoCache";
    private static final boolean DEBUG = App.DEBUG;

    private static final InfoCache INSTANCE = new InfoCache();
    private static final int MAX_ITEMS_ON_CACHE = 60;
    private static final int TRIM_CACHE_TO = 30;

    private static final LruCache<String, CacheData> LRU_CACHE = new LruCache<>(MAX_ITEMS_ON_CACHE);

    private InfoCache() {
        // Singleton instance
    }

    public enum CacheType {
        STREAM,
        CHANNEL,
        CHANNEL_TAB,
        COMMENTS,
        PLAYLIST,
        KIOSK
    }

    public static InfoCache getInstance() {
        return INSTANCE;
    }

    @NonNull
    private static String generateKey(final int serviceId,
                                     @NonNull final String url,
                                     @NonNull final CacheType cacheType) {
        return serviceId + ":" + cacheType.name() + ":" + url;
    }

    private static void removeExpiredEntries() {
        for (Map.Entry<String, CacheData> entry : LRU_CACHE.snapshot().entrySet()) {
            if (entry.getValue() != null && entry.getValue().isExpired()) {
                LRU_CACHE.remove(entry.getKey());
            }
        }
    }

    @Nullable
    public synchronized Info getInfo(final int serviceId,
                                    @NonNull final String url,
                                    @NonNull final CacheType cacheType) {
        if (DEBUG) Log.d(TAG, "Retrieving cache for: service=" + serviceId + ", url=" + url);
        
        final String key = generateKey(serviceId, url, cacheType);
        final CacheData data = LRU_CACHE.get(key);
        
        if (data == null) {
            if (DEBUG) Log.d(TAG, "Cache miss for key: " + key);
            return null;
        }

        if (data.isExpired()) {
            if (DEBUG) Log.d(TAG, "Cache expired for key: " + key);
            LRU_CACHE.remove(key);
            return null;
        }

        return data.info;
    }

    public synchronized void putInfo(final int serviceId,
                                     @NonNull final String url,
                                     @NonNull final Info info,
                                     @NonNull final CacheType cacheType) {
        if (DEBUG) Log.d(TAG, "Caching info: " + info.getName());
        
        final String key = generateKey(serviceId, url, cacheType);
        final long expirationMillis = getCacheTimeoutForService(info.getServiceId());
        LRU_CACHE.put(key, new CacheData(info, expirationMillis));
    }

    public synchronized void removeInfo(final int serviceId,
                                       @NonNull final String url,
                                       @NonNull final CacheType cacheType) {
        if (DEBUG) Log.d(TAG, "Removing cache for: service=" + serviceId + ", url=" + url);
        LRU_CACHE.remove(generateKey(serviceId, url, cacheType));
    }

    public synchronized void clearCache() {
        if (DEBUG) Log.d(TAG, "Clearing entire cache");
        LRU_CACHE.evictAll();
    }

    public synchronized void trimCache() {
        if (DEBUG) Log.d(TAG, "Trimming cache");
        removeExpiredEntries();
        LRU_CACHE.trimToSize(TRIM_CACHE_TO);
    }

    public synchronized long getSize() {
        return LRU_CACHE.size();
    }

    private long getCacheTimeoutForService(int serviceId) {
        // YouTube: 2 hours, other services: 1 hour
        return serviceId == 0 ? 7200000L : 3600000L;
    }

    private static final class CacheData {
        private final long expirationTime;
        private final Info info;

        CacheData(@NonNull final Info info, final long timeoutMillis) {
            this.info = info;
            this.expirationTime = System.currentTimeMillis() + timeoutMillis;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }
}