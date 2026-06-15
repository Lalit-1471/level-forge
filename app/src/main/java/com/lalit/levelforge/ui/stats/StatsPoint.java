package com.lalit.levelforge.ui.stats;

public class StatsPoint {

    private final String label;
    private final double value;

    public StatsPoint(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }
}
