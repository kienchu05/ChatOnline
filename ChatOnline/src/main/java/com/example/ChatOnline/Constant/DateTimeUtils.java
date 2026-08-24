package com.example.ChatOnline.Constant;

import java.time.Duration;
import java.time.Instant;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static String formatLastOnlineAt(Instant lastOnlineAt) {
        if (lastOnlineAt == null) return null;

        long minutes = Duration.between(lastOnlineAt, Instant.now()).toMinutes();

        if (minutes < 1)    return "Vừa hoạt động xong";
        if (minutes < 60)   return "Hoạt động " + minutes + " phút trước";
        if (minutes < 1440) return "Hoạt động " + (minutes / 60) + " giờ trước";
        return "Hoạt động " + (minutes / 1440) + " ngày trước";
    }
}