package com.lalit.levelforge.domain.progression;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.data.model.SetType;

public final class ExpCalculator {

    private ExpCalculator() {
    }

    public static int expForSet(Exercise exercise, WorkoutSet workoutSet, boolean progressiveOverload) {
        return breakdownForSet(exercise, workoutSet, progressiveOverload).getTotalExp();
    }

    public static ExpBreakdown breakdownForSet(Exercise exercise, WorkoutSet workoutSet, boolean progressiveOverload) {
        if (exercise == null || workoutSet == null || !workoutSet.isCompleted()) {
            return new ExpBreakdown(0, 0, 0, 0, 0);
        }

        double baseExp = Math.max(1, exercise.getBaseExp());
        double effortExp = volumeBonus(exercise.getExerciseType(), workoutSet);
        double subtotal = baseExp + effortExp;
        double setTypeBonus = subtotal * setTypeMultiplier(workoutSet.getSetType()) - subtotal;
        double overloadBonus = 0;

        if (progressiveOverload) {
            overloadBonus = (subtotal + setTypeBonus) * 0.20;
        }

        int roundedBase = Math.max(1, (int) Math.round(baseExp));
        int roundedEffort = Math.max(0, (int) Math.round(effortExp));
        int roundedSetType = (int) Math.round(setTypeBonus);
        int roundedOverload = Math.max(0, (int) Math.round(overloadBonus));
        int totalExp = Math.max(1, (int) Math.round(baseExp + effortExp + setTypeBonus + overloadBonus));
        return new ExpBreakdown(roundedBase, roundedEffort, roundedSetType, roundedOverload, totalExp);
    }

    private static double volumeBonus(ExerciseType exerciseType, WorkoutSet workoutSet) {
        if (exerciseType == null) {
            return 0;
        }

        switch (exerciseType) {
            case WEIGHT_REPS:
            case WEIGHTED_BODYWEIGHT:
                return (workoutSet.getWeightKg() * workoutSet.getReps()) / 25.0;
            case ASSISTED_BODYWEIGHT:
                return Math.max(0, workoutSet.getReps() * 1.5 - workoutSet.getAssistanceKg() / 10.0);
            case BODYWEIGHT_REPS:
                return workoutSet.getReps() * 1.6;
            case DURATION:
                return workoutSet.getDurationSeconds() / 12.0;
            case WEIGHT_DURATION:
                return (workoutSet.getWeightKg() / 4.0) + (workoutSet.getDurationSeconds() / 15.0);
            case DISTANCE_DURATION:
                return (workoutSet.getDistanceMeters() / 100.0) + (workoutSet.getDurationSeconds() / 30.0);
            case WEIGHT_DISTANCE:
                return (workoutSet.getWeightKg() / 5.0) + (workoutSet.getDistanceMeters() / 40.0);
            default:
                return 0;
        }
    }

    private static double setTypeMultiplier(SetType setType) {
        if (setType == null) {
            return 1.0;
        }
        switch (setType) {
            case WARMUP:
                return 0.55;
            case FAILURE:
            case AMRAP:
            case REST_PAUSE:
                return 1.20;
            case DROP_SET:
                return 1.15;
            case NORMAL:
            default:
                return 1.0;
        }
    }
}
