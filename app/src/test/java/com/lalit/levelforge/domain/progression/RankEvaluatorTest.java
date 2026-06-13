package com.lalit.levelforge.domain.progression;

import static org.junit.Assert.assertEquals;

import com.lalit.levelforge.data.model.RankTier;

import org.junit.Test;

public class RankEvaluatorTest {

    @Test
    public void rankForLevel_usesHunterTierThresholds() {
        assertEquals(RankTier.E, RankEvaluator.rankForLevel(1));
        assertEquals(RankTier.D, RankEvaluator.rankForLevel(10));
        assertEquals(RankTier.C, RankEvaluator.rankForLevel(20));
        assertEquals(RankTier.B, RankEvaluator.rankForLevel(35));
        assertEquals(RankTier.A, RankEvaluator.rankForLevel(55));
        assertEquals(RankTier.S, RankEvaluator.rankForLevel(75));
        assertEquals(RankTier.S_PLUS, RankEvaluator.rankForLevel(95));
    }
}
