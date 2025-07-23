package org.nidoham.stream.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgoFormatter {

    public static String format(Date uploadDate) {
        long now = System.currentTimeMillis();
        long time = uploadDate.getTime();
        long diff = now - time;

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "just now";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(30)) {
            long weeks = diff / TimeUnit.DAYS.toMillis(7);
            return weeks + " week" + (weeks > 1 ? "s" : "") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(365)) {
            long months = diff / TimeUnit.DAYS.toMillis(30);
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        } else {
            long years = diff / TimeUnit.DAYS.toMillis(365);
            return years + " year" + (years > 1 ? "s" : "") + " ago";
        }
    }
}