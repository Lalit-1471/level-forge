package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.QuestDefinition;

import java.util.List;

@Dao
public interface QuestDefinitionDao {

    @Query("SELECT * FROM quest_definitions WHERE active = 1 ORDER BY sortOrder")
    LiveData<List<QuestDefinition>> observeActiveDefinitions();

    @Query("SELECT * FROM quest_definitions WHERE active = 1 ORDER BY sortOrder")
    List<QuestDefinition> getActiveDefinitions();

    @Query("SELECT * FROM quest_definitions WHERE id = :questId LIMIT 1")
    QuestDefinition getDefinition(String questId);

    @Upsert
    void upsertAll(List<QuestDefinition> definitions);
}
