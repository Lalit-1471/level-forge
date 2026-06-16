package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "streak_state")
public class StreakState {

    @PrimaryKey
    private int id = 1;

    private int currentStreakDays;
    private int longestStreakDays;
    private int streakShields;
    private long lastLoginDayStartMillis;
    private long updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public void setCurrentStreakDays(int currentStreakDays) {
        this.currentStreakDays = currentStreakDays;
    }

    public int getLongestStreakDays() {
        return longestStreakDays;
    }

    public void setLongestStreakDays(int longestStreakDays) {
        this.longestStreakDays = longestStreakDays;
    }

    public int getStreakShields() {
        return streakShields;
    }

    public void setStreakShields(int streakShields) {
        this.streakShields = streakShields;
    }

    public long getLastLoginDayStartMillis() {
        return lastLoginDayStartMillis;
    }

    public void setLastLoginDayStartMillis(long lastLoginDayStartMillis) {
        this.lastLoginDayStartMillis = lastLoginDayStartMillis;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
