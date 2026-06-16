package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.QuestProgress;

import java.util.List;

@Dao
public interface QuestProgressDao {

    @Query("SELECT * FROM quest_progress WHERE periodStartMillis = :periodStartMillis")
    LiveData<List<QuestProgress>> observeProgressForPeriod(long periodStartMillis);

    @Query("SELECT * FROM quest_progress WHERE questId = :questId AND periodStartMillis = :periodStartMillis LIMIT 1")
    QuestProgress getProgress(String questId, long periodStartMillis);

    @Upsert
    void upsert(QuestProgress progress);
}
