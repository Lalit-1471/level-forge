package com.lalit.levelforge.ui.workout;

import com.lalit.levelforge.data.local.entity.Exercise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggedExercise {

    private final Exercise exercise;
    private final List<LoggedSet> sets;

    public LoggedExercise(Exercise exercise) {
        this(exercise, new ArrayList<>());
    }

    public LoggedExercise(Exercise exercise, List<LoggedSet> sets) {
        this.exercise = exercise;
        this.sets = new ArrayList<>(sets);
    }

    public Exercise getExercise() {
        return exercise;
    }

    public List<LoggedSet> getSets() {
        return Collections.unmodifiableList(sets);
    }
}
