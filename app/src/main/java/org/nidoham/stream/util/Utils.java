package org.nidoham.stream.util;

import android.content.Context;
import android.text.format.DateUtils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting various data types for display in the UI.
 */
public class Utils {

    /**
     * Formats a duration in milliseconds into a human-readable string (e.g., "05:30" or "1:20:00").
     * @param millis The duration in milliseconds.
     * @return Formatted duration string.
     */
    public static String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Formats a view count into a human-readable string (e.g., "1.2K", "5.6M", "1.3B").
     * @param views The raw view count.
     * @return Formatted view count string.
     */
    public static String formatViewCount(long views) {
        if (views < 1000) {
            return String.valueOf(views);
        }
        int exp = (int) (Math.log(views) / Math.log(1000));
        return String.format(Locale.getDefault(), "%.1f%c",
                views / Math.pow(1000, exp),
                "KMGTPE".charAt(exp - 1));
    }

    /**
     * Formats a timestamp into a relative time string (e.g., "2 hours ago", "3 days ago").
     * @param context The context to use for string resources.
     * @param timeMillis The timestamp in milliseconds.
     * @return Formatted relative time string.
     */
    public static CharSequence formatRelativeTime(Context context, long timeMillis) {
        return DateUtils.getRelativeTimeSpanString(timeMillis,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE);
    }
}