package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.local.relation.WorkoutSetWithExercise;

import java.util.List;

@Dao
public interface WorkoutSetDao {

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY setNumber")
    LiveData<List<WorkoutSet>> observeSetsForSession(long sessionId);

    @Transaction
    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY exerciseId, setNumber")
    LiveData<List<WorkoutSetWithExercise>> observeSetDetailsForSession(long sessionId);

    @Transaction
    @Query("SELECT workout_sets.* FROM workout_sets INNER JOIN workout_sessions ON workout_sets.sessionId = workout_sessions.id WHERE workout_sessions.completed = 1 ORDER BY workout_sessions.completedAt ASC, workout_sets.exerciseId, workout_sets.setNumber")
    LiveData<List<WorkoutSetWithExercise>> observeCompletedSetDetails();

    @Query("SELECT workout_sets.* FROM workout_sets INNER JOIN workout_sessions ON workout_sets.sessionId = workout_sessions.id WHERE workout_sets.exerciseId = :exerciseId AND workout_sets.completed = 1 AND workout_sessions.completed = 1 ORDER BY workout_sessions.completedAt DESC, workout_sets.setNumber ASC")
    List<WorkoutSet> getCompletedSetsForExercise(long exerciseId);

    @Insert
    long insert(WorkoutSet workoutSet);

    @Upsert
    void upsert(WorkoutSet workoutSet);
}
