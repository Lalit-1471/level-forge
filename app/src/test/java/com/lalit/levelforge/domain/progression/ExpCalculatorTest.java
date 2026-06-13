package com.lalit.levelforge.domain.progression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.data.model.MuscleGroup;
import com.lalit.levelforge.data.model.SetType;

import org.junit.Test;

public class ExpCalculatorTest {

    @Test
    public void expForSet_rewardsProgressiveOverload() {
        Exercise exercise = new Exercise("Bench Press", ExerciseType.WEIGHT_REPS, MuscleGroup.CHEST, "TRICEPS", 30, false);
        WorkoutSet set = new WorkoutSet();
        set.setCompleted(true);
        set.setSetType(SetType.NORMAL);
        set.setWeightKg(60);
        set.setReps(8);

        int normalExp = ExpCalculator.expForSet(exercise, set, false);
        int overloadExp = ExpCalculator.expForSet(exercise, set, true);

        assertTrue(overloadExp > normalExp);
    }

    @Test
    public void expForSet_ignoresIncompleteSets() {
        Exercise exercise = new Exercise("Bench Press", ExerciseType.WEIGHT_REPS, MuscleGroup.CHEST, "TRICEPS", 30, false);
        WorkoutSet set = new WorkoutSet();
        set.setCompleted(false);

        assertEquals(0, ExpCalculator.expForSet(exercise, set, false));
    }
}
