package com.lalit.levelforge.domain.progression;

import com.lalit.levelforge.data.model.RankTier;

public final class TitleCatalog {

    private TitleCatalog() {
    }

    public static String titleFor(int level, RankTier rankTier) {
        if (rankTier == RankTier.S_PLUS) {
            return "Monarch Candidate";
        }
        if (rankTier == RankTier.S) {
            return "Dungeon Breaker";
        }
        if (rankTier == RankTier.A) {
            return "Elite Hunter";
        }
        if (rankTier == RankTier.B) {
            return "Raid Captain";
        }
        if (rankTier == RankTier.C) {
            return "Gate Walker";
        }
        if (rankTier == RankTier.D) {
            return "Rising Hunter";
        }
        if (level >= 5) {
            return "Awakened Hunter";
        }
        return "Novice Hunter";
    }
}
