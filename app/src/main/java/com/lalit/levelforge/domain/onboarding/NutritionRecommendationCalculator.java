package com.lalit.levelforge.domain.onboarding;

import com.lalit.levelforge.data.local.entity.UserGoal;
import com.lalit.levelforge.data.local.entity.UserProfile;
import com.lalit.levelforge.data.model.ActivityLevel;
import com.lalit.levelforge.data.model.Gender;
import com.lalit.levelforge.data.model.GoalType;

public final class NutritionRecommendationCalculator {

    private NutritionRecommendationCalculator() {
    }

    public static NutritionRecommendation calculate(UserProfile profile) {
        if (profile == null) {
            return new NutritionRecommendation(2200, 140, 250, 65, 3.0, 30, 45);
        }

        UserGoal goal = profile.getUserGoal();
        GoalType goalType = goal == null ? GoalType.BUILD_MUSCLE : goal.getPrimaryGoal();
        ActivityLevel activityLevel = goal == null ? ActivityLevel.MODERATELY_ACTIVE : goal.getActivityLevel();
        double bmr = estimateBmr(profile);
        int calories = roundToNearestTen((int) Math.round((bmr * activityMultiplier(activityLevel)) + goalAdjustment(goalType)));
        calories = Math.max(1400, calories);

        int protein = (int) Math.round(profile.getWeightKg() * proteinMultiplier(goalType));
        int fat = Math.max(45, (int) Math.round((calories * 0.25) / 9.0));
        int carbs = Math.max(100, (int) Math.round((calories - (protein * 4.0) - (fat * 9.0)) / 4.0));
        double water = Math.max(2.5, Math.round(profile.getWeightKg() * 0.04 * 10.0) / 10.0);
        int fiber = Math.max(25, (int) Math.round(calories / 100.0 * 1.4));
        int workoutDuration = workoutDurationMinutes(activityLevel, goalType);

        return new NutritionRecommendation(calories, protein, carbs, fat, water, fiber, workoutDuration);
    }

    private static double estimateBmr(UserProfile profile) {
        double base = (10.0 * profile.getWeightKg()) + (6.25 * profile.getHeightCm()) - (5.0 * profile.getAge());
        if (profile.getGender() == Gender.FEMALE) {
            return base - 161.0;
        }
        if (profile.getGender() == Gender.MALE) {
            return base + 5.0;
        }
        return base - 78.0;
    }

    private static double activityMultiplier(ActivityLevel activityLevel) {
        if (activityLevel == null) {
            return 1.55;
        }
        switch (activityLevel) {
            case SEDENTARY:
                return 1.20;
            case LIGHTLY_ACTIVE:
                return 1.375;
            case VERY_ACTIVE:
                return 1.725;
            case MODERATELY_ACTIVE:
            default:
                return 1.55;
        }
    }

    private static double goalAdjustment(GoalType goalType) {
        if (goalType == GoalType.LOSE_WEIGHT) {
            return -350;
        }
        if (goalType == GoalType.BUILD_MUSCLE) {
            return 250;
        }
        return 0;
    }

    private static double proteinMultiplier(GoalType goalType) {
        if (goalType == GoalType.LOSE_WEIGHT) {
            return 2.0;
        }
        if (goalType == GoalType.BUILD_MUSCLE) {
            return 1.9;
        }
        return 1.7;
    }

    private static int workoutDurationMinutes(ActivityLevel activityLevel, GoalType goalType) {
        int base = activityLevel == ActivityLevel.SEDENTARY ? 35 : 45;
        if (goalType == GoalType.BUILD_MUSCLE) {
            return base + 15;
        }
        if (goalType == GoalType.LOSE_WEIGHT) {
            return base + 10;
        }
        return base;
    }

    private static int roundToNearestTen(int value) {
        return Math.round(value / 10.0f) * 10;
    }
}
