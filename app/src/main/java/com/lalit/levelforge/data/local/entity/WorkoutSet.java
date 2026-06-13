package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.SetType;

@Entity(
        tableName = "workout_sets",
        foreignKeys = {
                @ForeignKey(
                        entity = WorkoutSession.class,
                        parentColumns = "id",
                        childColumns = "sessionId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Exercise.class,
                        parentColumns = "id",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("sessionId"),
                @Index("exerciseId")
        }
)
public class WorkoutSet {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long sessionId;
    private long exerciseId;
    private int setNumber;
    private SetType setType;
    private int reps;
    private double weightKg;
    private int durationSeconds;
    private double distanceMeters;
    private double assistanceKg;
    private boolean completed;

    public WorkoutSet() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public SetType getSetType() {
        return setType;
    }

    public void setSetType(SetType setType) {
        this.setType = setType;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public double getAssistanceKg() {
        return assistanceKg;
    }

    public void setAssistanceKg(double assistanceKg) {
        this.assistanceKg = assistanceKg;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
