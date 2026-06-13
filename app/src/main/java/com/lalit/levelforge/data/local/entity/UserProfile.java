package com.lalit.levelforge.data.local.entity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.Gender;

@Entity(tableName = "user_profiles")
public class UserProfile {

    @PrimaryKey
    private long id = 1L;

    private Gender gender;
    private int age;
    private double heightCm;
    private double weightKg;
    private String healthFlags;
    private long createdAt;
    private long updatedAt;
    private boolean onboardingComplete;
    private boolean nutritionRecommendationShown;

    @Embedded(prefix = "goal_")
    private UserGoal userGoal;

    @Embedded(prefix = "baseline_")
    private StrengthBaseline strengthBaseline;

    public UserProfile() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public String getHealthFlags() {
        return healthFlags;
    }

    public void setHealthFlags(String healthFlags) {
        this.healthFlags = healthFlags;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isOnboardingComplete() {
        return onboardingComplete;
    }

    public void setOnboardingComplete(boolean onboardingComplete) {
        this.onboardingComplete = onboardingComplete;
    }

    public boolean isNutritionRecommendationShown() {
        return nutritionRecommendationShown;
    }

    public void setNutritionRecommendationShown(boolean nutritionRecommendationShown) {
        this.nutritionRecommendationShown = nutritionRecommendationShown;
    }

    public UserGoal getUserGoal() {
        return userGoal;
    }

    public void setUserGoal(UserGoal userGoal) {
        this.userGoal = userGoal;
    }

    public StrengthBaseline getStrengthBaseline() {
        return strengthBaseline;
    }

    public void setStrengthBaseline(StrengthBaseline strengthBaseline) {
        this.strengthBaseline = strengthBaseline;
    }
}
