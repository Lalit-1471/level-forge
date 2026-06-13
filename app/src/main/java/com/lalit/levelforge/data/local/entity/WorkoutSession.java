package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_sessions")
public class WorkoutSession {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private int totalExp;
    private long completedAt;

    public WorkoutSession(String title, int totalExp, long completedAt) {
        this.title = title;
        this.totalExp = totalExp;
        this.completedAt = completedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalExp() {
        return totalExp;
    }

    public long getCompletedAt() {
        return completedAt;
    }
}

