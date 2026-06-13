package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_sessions")
public class WorkoutSession {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private long startedAt;
    private long completedAt;
    private int durationSeconds;
    private int totalExp;
    private String notes;
    private boolean completed;
    private long createdAt;
    private long updatedAt;

    @Ignore
    public WorkoutSession(String title, int totalExp, long completedAt) {
        this.title = title;
        this.totalExp = totalExp;
        this.completedAt = completedAt;
        this.startedAt = completedAt;
        this.completed = true;
        this.createdAt = completedAt;
        this.updatedAt = completedAt;
    }

    public WorkoutSession() {
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

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public int getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(int totalExp) {
        this.totalExp = totalExp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
