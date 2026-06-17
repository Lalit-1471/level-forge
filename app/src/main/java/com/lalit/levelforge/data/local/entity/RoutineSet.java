package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.SetType;

@Entity(
        tableName = "routine_sets",
        foreignKeys = {
                @ForeignKey(
                        entity = RoutineExercise.class,
                        parentColumns = "id",
                        childColumns = "routineExerciseId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("routineExerciseId")
        }
)
public class RoutineSet {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long routineExerciseId;
    private int setNumber;
    private SetType setType;
    private int reps;
    private double weightKg;
    private int durationSeconds;
    private double distanceMeters;
    private double assistanceKg;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoutineExerciseId() {
        return routineExerciseId;
    }

    public void setRoutineExerciseId(long routineExerciseId) {
        this.routineExerciseId = routineExerciseId;
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
}
