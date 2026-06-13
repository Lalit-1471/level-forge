package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.lalit.levelforge.data.local.entity.WorkoutSession;

import java.util.List;

@Dao
public interface WorkoutSessionDao {

    @Query("SELECT * FROM workout_sessions ORDER BY completedAt DESC LIMIT 5")
    LiveData<List<WorkoutSession>> getRecentSessions();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WorkoutSession workoutSession);
}

