package com.lalit.levelforge.domain.progression;

import com.lalit.levelforge.data.model.RankTier;

public final class RankEvaluator {

    private RankEvaluator() {
    }

    public static RankTier rankForLevel(int level) {
        if (level >= 95) {
            return RankTier.S_PLUS;
        }
        if (level >= 75) {
            return RankTier.S;
        }
        if (level >= 55) {
            return RankTier.A;
        }
        if (level >= 35) {
            return RankTier.B;
        }
        if (level >= 20) {
            return RankTier.C;
        }
        if (level >= 10) {
            return RankTier.D;
        }
        return RankTier.E;
    }
}
