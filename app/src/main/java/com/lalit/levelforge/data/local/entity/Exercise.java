package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.data.model.MuscleGroup;

@Entity(tableName = "exercises")
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private ExerciseType exerciseType;
    private MuscleGroup primaryMuscleGroup;
    private String secondaryMuscleGroups;
    private int baseExp;
    private boolean custom;

    public Exercise() {
    }

    @Ignore
    public Exercise(String name, ExerciseType exerciseType, MuscleGroup primaryMuscleGroup,
                    String secondaryMuscleGroups, int baseExp, boolean custom) {
        this.name = name;
        this.exerciseType = exerciseType;
        this.primaryMuscleGroup = primaryMuscleGroup;
        this.secondaryMuscleGroups = secondaryMuscleGroups;
        this.baseExp = baseExp;
        this.custom = custom;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }

    public MuscleGroup getPrimaryMuscleGroup() {
        return primaryMuscleGroup;
    }

    public void setPrimaryMuscleGroup(MuscleGroup primaryMuscleGroup) {
        this.primaryMuscleGroup = primaryMuscleGroup;
    }

    public String getSecondaryMuscleGroups() {
        return secondaryMuscleGroups;
    }

    public void setSecondaryMuscleGroups(String secondaryMuscleGroups) {
        this.secondaryMuscleGroups = secondaryMuscleGroups;
    }

    public int getBaseExp() {
        return baseExp;
    }

    public void setBaseExp(int baseExp) {
        this.baseExp = baseExp;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }
}
