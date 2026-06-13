package com.lalit.levelforge.data.seed;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.data.model.MuscleGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DefaultExercises {

    private DefaultExercises() {
    }

    public static List<Exercise> create() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("Barbell Bench Press", ExerciseType.WEIGHT_REPS, MuscleGroup.CHEST, "TRICEPS,SHOULDERS", 32, false));
        exercises.add(new Exercise("Incline Dumbbell Press", ExerciseType.WEIGHT_REPS, MuscleGroup.CHEST, "SHOULDERS,TRICEPS", 30, false));
        exercises.add(new Exercise("Push Up", ExerciseType.BODYWEIGHT_REPS, MuscleGroup.CHEST, "TRICEPS,CORE", 18, false));
        exercises.add(new Exercise("Pull Up", ExerciseType.BODYWEIGHT_REPS, MuscleGroup.BACK, "BICEPS,CORE", 28, false));
        exercises.add(new Exercise("Assisted Pull Up", ExerciseType.ASSISTED_BODYWEIGHT, MuscleGroup.BACK, "BICEPS", 22, false));
        exercises.add(new Exercise("Barbell Row", ExerciseType.WEIGHT_REPS, MuscleGroup.BACK, "BICEPS,CORE", 32, false));
        exercises.add(new Exercise("Lat Pulldown", ExerciseType.WEIGHT_REPS, MuscleGroup.BACK, "BICEPS", 28, false));
        exercises.add(new Exercise("Overhead Press", ExerciseType.WEIGHT_REPS, MuscleGroup.SHOULDERS, "TRICEPS,CORE", 31, false));
        exercises.add(new Exercise("Lateral Raise", ExerciseType.WEIGHT_REPS, MuscleGroup.SHOULDERS, "", 20, false));
        exercises.add(new Exercise("Barbell Curl", ExerciseType.WEIGHT_REPS, MuscleGroup.BICEPS, "FOREARMS", 18, false));
        exercises.add(new Exercise("Triceps Pushdown", ExerciseType.WEIGHT_REPS, MuscleGroup.TRICEPS, "", 18, false));
        exercises.add(new Exercise("Back Squat", ExerciseType.WEIGHT_REPS, MuscleGroup.QUADS, "GLUTES,HAMSTRINGS,CORE", 38, false));
        exercises.add(new Exercise("Deadlift", ExerciseType.WEIGHT_REPS, MuscleGroup.HAMSTRINGS, "BACK,GLUTES,CORE", 42, false));
        exercises.add(new Exercise("Romanian Deadlift", ExerciseType.WEIGHT_REPS, MuscleGroup.HAMSTRINGS, "GLUTES,BACK", 34, false));
        exercises.add(new Exercise("Leg Press", ExerciseType.WEIGHT_REPS, MuscleGroup.QUADS, "GLUTES,HAMSTRINGS", 30, false));
        exercises.add(new Exercise("Standing Calf Raise", ExerciseType.WEIGHT_REPS, MuscleGroup.CALVES, "", 16, false));
        exercises.add(new Exercise("Plank", ExerciseType.DURATION, MuscleGroup.CORE, "FULL_BODY", 16, false));
        exercises.add(new Exercise("Running", ExerciseType.DISTANCE_DURATION, MuscleGroup.CARDIO, "FULL_BODY", 20, false));
        return Collections.unmodifiableList(exercises);
    }
}
