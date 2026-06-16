package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "quest_progress",
        indices = {
                @Index(value = {"questId", "periodStartMillis"}, unique = true),
                @Index("periodStartMillis")
        }
)
public class QuestProgress {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String questId;
    private long periodStartMillis;
    private int progressCount;
    private boolean completed;
    private boolean rewardClaimed;
    private long updatedAt;

    public QuestProgress() {
    }

    @Ignore
    public QuestProgress(String questId, long periodStartMillis, int progressCount,
                         boolean completed, boolean rewardClaimed, long updatedAt) {
        this.questId = questId;
        this.periodStartMillis = periodStartMillis;
        this.progressCount = progressCount;
        this.completed = completed;
        this.rewardClaimed = rewardClaimed;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    public void setRewardClaimed(boolean rewardClaimed) {
        this.rewardClaimed = rewardClaimed;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
