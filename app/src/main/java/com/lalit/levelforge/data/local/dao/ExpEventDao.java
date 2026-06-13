package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.lalit.levelforge.data.local.entity.ExpEvent;

import java.util.List;

@Dao
public interface ExpEventDao {

    @Query("SELECT COALESCE(SUM(amount), 0) FROM exp_events")
    LiveData<Integer> observeTotalExp();

    @Query("SELECT * FROM exp_events ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<ExpEvent>> observeRecentEvents(int limit);

    @Insert
    long insert(ExpEvent expEvent);
}
