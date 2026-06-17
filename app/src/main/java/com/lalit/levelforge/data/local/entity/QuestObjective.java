package com.lalit.levelforge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.QuestMetricType;

@Entity(
        tableName = "quest_objectives",
        indices = {
                @Index("questId"),
                @Index("sortOrder")
        }
)
public class QuestObjective {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String questId;
    private String label;
    private QuestMetricType metricType;
    private int targetCount;
    private int sortOrder;

    public QuestObjective() {
    }

    @Ignore
    public QuestObjective(@NonNull String id, String questId, String label,
                          QuestMetricType metricType, int targetCount, int sortOrder) {
        this.id = id;
        this.questId = questId;
        this.label = label;
        this.metricType = metricType;
        this.targetCount = targetCount;
        this.sortOrder = sortOrder;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public QuestMetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(QuestMetricType metricType) {
        this.metricType = metricType;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
