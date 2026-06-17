package com.lalit.levelforge.domain.calendar;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class TrainingCalendar {

    private TrainingCalendar() {
    }

    public static long startOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long startOfWeek(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startOfDay(millis));
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return calendar.getTimeInMillis();
    }

    public static long endOfDay(long millis) {
        return startOfDay(millis) + TimeUnit.DAYS.toMillis(1);
    }

    public static long endOfWeek(long millis) {
        return startOfWeek(millis) + TimeUnit.DAYS.toMillis(7);
    }

    public static long startOfBiweek(long millis) {
        long weekStart = startOfWeek(millis);
        long weekIndex = TimeUnit.MILLISECONDS.toDays(weekStart) / 7;
        if (weekIndex % 2 == 0) {
            return weekStart;
        }
        return weekStart - TimeUnit.DAYS.toMillis(7);
    }

    public static boolean isSameDay(long firstMillis, long secondMillis) {
        return startOfDay(firstMillis) == startOfDay(secondMillis);
    }

    public static boolean isSameWeek(long firstMillis, long secondMillis) {
        return startOfWeek(firstMillis) == startOfWeek(secondMillis);
    }

    public static int daysBetween(long olderMillis, long newerMillis) {
        long olderDay = startOfDay(olderMillis);
        long newerDay = startOfDay(newerMillis);
        return (int) TimeUnit.MILLISECONDS.toDays(newerDay - olderDay);
    }
}
