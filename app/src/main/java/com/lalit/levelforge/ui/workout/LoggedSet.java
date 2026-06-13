package com.lalit.levelforge.ui.workout;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.WorkoutSet;

public class LoggedSet {

    private final Exercise exercise;
    private final WorkoutSet workoutSet;
    private final int exp;

    public LoggedSet(Exercise exercise, WorkoutSet workoutSet, int exp) {
        this.exercise = exercise;
        this.workoutSet = workoutSet;
        this.exp = exp;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public WorkoutSet getWorkoutSet() {
        return workoutSet;
    }

    public int getExp() {
        return exp;
    }
}
