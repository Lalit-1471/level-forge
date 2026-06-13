package com.lalit.levelforge.domain.progression;

public final class LevelCurve {

    private LevelCurve() {
    }

    public static int expToAdvanceFromLevel(int level) {
        int safeLevel = Math.max(1, level);
        if (safeLevel <= 5) {
            return 120 + ((safeLevel - 1) * 40);
        }
        if (safeLevel <= 15) {
            return 300 + ((safeLevel - 6) * 75);
        }
        if (safeLevel <= 35) {
            return 1050 + ((safeLevel - 16) * 125);
        }
        return 3550 + ((safeLevel - 36) * 225);
    }

    public static int levelForTotalExp(int totalExp) {
        int remainingExp = Math.max(0, totalExp);
        int level = 1;
        while (remainingExp >= expToAdvanceFromLevel(level)) {
            remainingExp -= expToAdvanceFromLevel(level);
            level++;
        }
        return level;
    }

    public static int expIntoCurrentLevel(int totalExp) {
        int remainingExp = Math.max(0, totalExp);
        int level = 1;
        while (remainingExp >= expToAdvanceFromLevel(level)) {
            remainingExp -= expToAdvanceFromLevel(level);
            level++;
        }
        return remainingExp;
    }

    public static int expToNextLevel(int totalExp) {
        int level = levelForTotalExp(totalExp);
        return expToAdvanceFromLevel(level) - expIntoCurrentLevel(totalExp);
    }
}
