package com.lalit.levelforge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.QuestMetricType;
import com.lalit.levelforge.data.model.QuestResetType;
import com.lalit.levelforge.data.model.QuestRewardType;

@Entity(tableName = "quest_definitions")
public class QuestDefinition {

    @PrimaryKey
    @NonNull
    private String id = "";

    private String title;
    private String description;
    private QuestResetType resetType;
    private QuestMetricType metricType;
    private int targetCount;
    private QuestRewardType rewardType;
    private int rewardAmount;
    private boolean active;
    private int sortOrder;

    public QuestDefinition() {
    }

    @Ignore
    public QuestDefinition(@NonNull String id, String title, String description,
                           QuestResetType resetType, QuestMetricType metricType,
                           int targetCount, QuestRewardType rewardType,
                           int rewardAmount, boolean active, int sortOrder) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.resetType = resetType;
        this.metricType = metricType;
        this.targetCount = targetCount;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
        this.active = active;
        this.sortOrder = sortOrder;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QuestResetType getResetType() {
        return resetType;
    }

    public void setResetType(QuestResetType resetType) {
        this.resetType = resetType;
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

    public QuestRewardType getRewardType() {
        return rewardType;
    }

    public void setRewardType(QuestRewardType rewardType) {
        this.rewardType = rewardType;
    }

    public int getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(int rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
