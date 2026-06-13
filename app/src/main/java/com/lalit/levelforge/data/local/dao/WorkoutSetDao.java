package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.WorkoutSet;

import java.util.List;

@Dao
public interface WorkoutSetDao {

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY setNumber")
    LiveData<List<WorkoutSet>> observeSetsForSession(long sessionId);

    @Insert
    long insert(WorkoutSet workoutSet);

    @Upsert
    void upsert(WorkoutSet workoutSet);
}
