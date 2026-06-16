package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.ProgressionEventType;

@Entity(
        tableName = "progression_events",
        indices = {
                @Index("eventType"),
                @Index("sessionId"),
                @Index("exerciseId"),
                @Index("createdAt")
        }
)
public class ProgressionEvent {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private ProgressionEventType eventType;
    private long sessionId;
    private long workoutSetId;
    private long exerciseId;
    private double value;
    private String label;
    private long createdAt;

    public ProgressionEvent() {
    }

    @Ignore
    public ProgressionEvent(ProgressionEventType eventType, long sessionId, long workoutSetId,
                            long exerciseId, double value, String label, long createdAt) {
        this.eventType = eventType;
        this.sessionId = sessionId;
        this.workoutSetId = workoutSetId;
        this.exerciseId = exerciseId;
        this.value = value;
        this.label = label;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProgressionEventType getEventType() {
        return eventType;
    }

    public void setEventType(ProgressionEventType eventType) {
        this.eventType = eventType;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getWorkoutSetId() {
        return workoutSetId;
    }

    public void setWorkoutSetId(long workoutSetId) {
        this.workoutSetId = workoutSetId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
