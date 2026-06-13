package com.lalit.levelforge.data.local.entity;

import androidx.room.Ignore;

import com.lalit.levelforge.data.model.ActivityLevel;
import com.lalit.levelforge.data.model.GoalType;

public class UserGoal {

    private GoalType primaryGoal;
    private double targetWeightKg;
    private ActivityLevel activityLevel;

    public UserGoal() {
    }

    @Ignore
    public UserGoal(GoalType primaryGoal, double targetWeightKg, ActivityLevel activityLevel) {
        this.primaryGoal = primaryGoal;
        this.targetWeightKg = targetWeightKg;
        this.activityLevel = activityLevel;
    }

    public GoalType getPrimaryGoal() {
        return primaryGoal;
    }

    public void setPrimaryGoal(GoalType primaryGoal) {
        this.primaryGoal = primaryGoal;
    }

    public double getTargetWeightKg() {
        return targetWeightKg;
    }

    public void setTargetWeightKg(double targetWeightKg) {
        this.targetWeightKg = targetWeightKg;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }
}
