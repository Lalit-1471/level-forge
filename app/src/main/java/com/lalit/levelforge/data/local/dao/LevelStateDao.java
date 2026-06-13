package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.LevelState;

@Dao
public interface LevelStateDao {

    @Query("SELECT * FROM level_state WHERE id = 1")
    LiveData<LevelState> observeLevelState();

    @Query("SELECT * FROM level_state WHERE id = 1")
    LevelState getLevelState();

    @Upsert
    void upsert(LevelState levelState);
}
