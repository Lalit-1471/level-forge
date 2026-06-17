package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.QuestObjectiveProgress;

import java.util.List;

@Dao
public interface QuestObjectiveProgressDao {

    @Query("SELECT * FROM quest_objective_progress WHERE periodStartMillis = :periodStartMillis")
    LiveData<List<QuestObjectiveProgress>> observeProgressForPeriod(long periodStartMillis);

    @Query("SELECT * FROM quest_objective_progress WHERE objectiveId = :objectiveId AND periodStartMillis = :periodStartMillis LIMIT 1")
    QuestObjectiveProgress getProgress(String objectiveId, long periodStartMillis);

    @Query("SELECT * FROM quest_objective_progress WHERE questId = :questId AND periodStartMillis = :periodStartMillis")
    List<QuestObjectiveProgress> getProgressForQuest(String questId, long periodStartMillis);

    @Upsert
    void upsert(QuestObjectiveProgress progress);
}
