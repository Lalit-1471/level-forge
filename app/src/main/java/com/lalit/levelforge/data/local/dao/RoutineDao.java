package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.lalit.levelforge.data.local.entity.Routine;
import com.lalit.levelforge.data.local.entity.RoutineExercise;
import com.lalit.levelforge.data.local.entity.RoutineSet;

import java.util.List;

@Dao
public interface RoutineDao {

    @Query("SELECT * FROM routines ORDER BY updatedAt DESC")
    LiveData<List<Routine>> observeRoutines();

    @Insert
    long insertRoutine(Routine routine);

    @Insert
    long insertRoutineExercise(RoutineExercise routineExercise);

    @Insert
    long insertRoutineSet(RoutineSet routineSet);

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY sortOrder")
    List<RoutineExercise> getRoutineExercises(long routineId);

    @Query("SELECT * FROM routine_sets WHERE routineExerciseId = :routineExerciseId ORDER BY setNumber")
    List<RoutineSet> getRoutineSets(long routineExerciseId);

    @Query("DELETE FROM routines WHERE id = :routineId")
    void deleteRoutine(long routineId);
}
