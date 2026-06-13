package com.lalit.levelforge.data.local.entity;

public class StrengthBaseline {

    private int maxPushups;
    private int maxPullups;
    private int maxBodyweightSquats;
    private double squatKg;
    private double benchKg;
    private double deadliftKg;
    private double overheadPressKg;

    public StrengthBaseline() {
    }

    public int getMaxPushups() {
        return maxPushups;
    }

    public void setMaxPushups(int maxPushups) {
        this.maxPushups = maxPushups;
    }

    public int getMaxPullups() {
        return maxPullups;
    }

    public void setMaxPullups(int maxPullups) {
        this.maxPullups = maxPullups;
    }

    public int getMaxBodyweightSquats() {
        return maxBodyweightSquats;
    }

    public void setMaxBodyweightSquats(int maxBodyweightSquats) {
        this.maxBodyweightSquats = maxBodyweightSquats;
    }

    public double getSquatKg() {
        return squatKg;
    }

    public void setSquatKg(double squatKg) {
        this.squatKg = squatKg;
    }

    public double getBenchKg() {
        return benchKg;
    }

    public void setBenchKg(double benchKg) {
        this.benchKg = benchKg;
    }

    public double getDeadliftKg() {
        return deadliftKg;
    }

    public void setDeadliftKg(double deadliftKg) {
        this.deadliftKg = deadliftKg;
    }

    public double getOverheadPressKg() {
        return overheadPressKg;
    }

    public void setOverheadPressKg(double overheadPressKg) {
        this.overheadPressKg = overheadPressKg;
    }
}
