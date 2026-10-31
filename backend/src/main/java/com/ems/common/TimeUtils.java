package com.ems.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


public final class TimeUtils {

    private TimeUtils() {}

    public static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.atZone(ZoneId.systemDefault()).toInstant();
    }
}
