package com.lalit.levelforge.domain.progression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LevelCurveTest {

    @Test
    public void levelForTotalExp_levelsUpQuicklyAtStart() {
        assertEquals(1, LevelCurve.levelForTotalExp(0));
        assertEquals(2, LevelCurve.levelForTotalExp(120));
        assertEquals(3, LevelCurve.levelForTotalExp(280));
    }

    @Test
    public void expToAdvanceFromLevel_increasesOverTime() {
        assertTrue(LevelCurve.expToAdvanceFromLevel(10) > LevelCurve.expToAdvanceFromLevel(2));
        assertTrue(LevelCurve.expToAdvanceFromLevel(40) > LevelCurve.expToAdvanceFromLevel(10));
    }
}
