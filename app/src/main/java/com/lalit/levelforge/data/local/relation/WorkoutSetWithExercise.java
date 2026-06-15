package com.lalit.levelforge.data.local.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.WorkoutSet;

public class WorkoutSetWithExercise {

    @Embedded
    private WorkoutSet workoutSet;

    @Relation(parentColumn = "exerciseId", entityColumn = "id")
    private Exercise exercise;

    public WorkoutSet getWorkoutSet() {
        return workoutSet;
    }

    public void setWorkoutSet(WorkoutSet workoutSet) {
        this.workoutSet = workoutSet;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
}
