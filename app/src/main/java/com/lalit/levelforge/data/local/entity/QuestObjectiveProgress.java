package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "quest_objective_progress",
        indices = {
                @Index(value = {"objectiveId", "periodStartMillis"}, unique = true),
                @Index("questId"),
                @Index("periodStartMillis")
        }
)
public class QuestObjectiveProgress {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String objectiveId;
    private String questId;
    private long periodStartMillis;
    private int progressCount;
    private boolean completed;
    private long updatedAt;

    public QuestObjectiveProgress() {
    }

    @Ignore
    public QuestObjectiveProgress(String objectiveId, String questId, long periodStartMillis,
                                  int progressCount, boolean completed, long updatedAt) {
        this.objectiveId = objectiveId;
        this.questId = questId;
        this.periodStartMillis = periodStartMillis;
        this.progressCount = progressCount;
        this.completed = completed;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(String objectiveId) {
        this.objectiveId = objectiveId;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public long getPeriodStartMillis() {
        return periodStartMillis;
    }

    public void setPeriodStartMillis(long periodStartMillis) {
        this.periodStartMillis = periodStartMillis;
    }

    public int getProgressCount() {
        return progressCount;
    }

    public void setProgressCount(int progressCount) {
        this.progressCount = progressCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
