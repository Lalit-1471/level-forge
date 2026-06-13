package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.lalit.levelforge.data.model.ExpSourceType;

@Entity(tableName = "exp_events")
public class ExpEvent {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private ExpSourceType sourceType;
    private long sourceId;
    private int amount;
    private String reason;
    private long createdAt;

    public ExpEvent() {
    }

    @Ignore
    public ExpEvent(ExpSourceType sourceType, long sourceId, int amount, String reason, long createdAt) {
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ExpSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ExpSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public long getSourceId() {
        return sourceId;
    }

    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
