package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.StreakState;

@Dao
public interface StreakStateDao {

    @Query("SELECT * FROM streak_state WHERE id = 1 LIMIT 1")
    LiveData<StreakState> observeStreakState();

    @Query("SELECT * FROM streak_state WHERE id = 1 LIMIT 1")
    StreakState getStreakState();

    @Upsert
    void upsert(StreakState streakState);
}
