package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.RankTier;

@Entity(tableName = "level_state")
public class LevelState {

    @PrimaryKey
    private long id = 1L;

    private int level;
    private int totalExp;
    private RankTier rankTier;
    private String activeTitle;
    private long updatedAt;

    public LevelState() {
    }

    @Ignore
    public LevelState(int level, int totalExp, RankTier rankTier, String activeTitle, long updatedAt) {
        this.level = level;
        this.totalExp = totalExp;
        this.rankTier = rankTier;
        this.activeTitle = activeTitle;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(int totalExp) {
        this.totalExp = totalExp;
    }

    public RankTier getRankTier() {
        return rankTier;
    }

    public void setRankTier(RankTier rankTier) {
        this.rankTier = rankTier;
    }

    public String getActiveTitle() {
        return activeTitle;
    }

    public void setActiveTitle(String activeTitle) {
        this.activeTitle = activeTitle;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
