package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.QuestObjective;

import java.util.List;

@Dao
public interface QuestObjectiveDao {

    @Query("SELECT * FROM quest_objectives ORDER BY sortOrder")
    LiveData<List<QuestObjective>> observeObjectives();

    @Query("SELECT * FROM quest_objectives WHERE questId = :questId ORDER BY sortOrder")
    List<QuestObjective> getObjectivesForQuest(String questId);

    @Upsert
    void upsertAll(List<QuestObjective> objectives);
}
