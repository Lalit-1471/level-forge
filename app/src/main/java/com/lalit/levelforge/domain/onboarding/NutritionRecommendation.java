package com.lalit.levelforge.domain.onboarding;

public class NutritionRecommendation {

    private final int calories;
    private final int proteinGrams;
    private final int carbsGrams;
    private final int fatGrams;
    private final double waterLiters;
    private final int fiberGrams;
    private final int workoutDurationMinutes;

    public NutritionRecommendation(int calories, int proteinGrams, int carbsGrams, int fatGrams,
                                   double waterLiters, int fiberGrams, int workoutDurationMinutes) {
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
        this.waterLiters = waterLiters;
        this.fiberGrams = fiberGrams;
        this.workoutDurationMinutes = workoutDurationMinutes;
    }

    public int getCalories() {
        return calories;
    }

    public int getProteinGrams() {
        return proteinGrams;
    }

    public int getCarbsGrams() {
        return carbsGrams;
    }

    public int getFatGrams() {
        return fatGrams;
    }

    public double getWaterLiters() {
        return waterLiters;
    }

    public int getFiberGrams() {
        return fiberGrams;
    }

    public int getWorkoutDurationMinutes() {
        return workoutDurationMinutes;
    }
}
