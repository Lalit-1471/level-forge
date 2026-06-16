package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.lalit.levelforge.data.local.entity.ProgressionEvent;
import com.lalit.levelforge.data.model.ProgressionEventType;

import java.util.List;

@Dao
public interface ProgressionEventDao {

    @Insert
    long insert(ProgressionEvent progressionEvent);

    @Query("SELECT * FROM progression_events ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<ProgressionEvent>> observeRecentEvents(int limit);

    @Query("SELECT * FROM progression_events WHERE createdAt >= :startMillis AND createdAt < :endMillis ORDER BY createdAt DESC")
    LiveData<List<ProgressionEvent>> observeEventsBetween(long startMillis, long endMillis);

    @Query("SELECT COUNT(*) FROM progression_events WHERE eventType = :eventType AND createdAt >= :startMillis AND createdAt < :endMillis")
    int countEventsBetween(ProgressionEventType eventType, long startMillis, long endMillis);
}
