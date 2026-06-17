package com.lalit.levelforge.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "routine_exercises",
        foreignKeys = {
                @ForeignKey(
                        entity = Routine.class,
                        parentColumns = "id",
                        childColumns = "routineId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Exercise.class,
                        parentColumns = "id",
                        childColumns = "exerciseId",
                        onDelete = ForeignKey.RESTRICT
                )
        },
        indices = {
                @Index("routineId"),
                @Index("exerciseId")
        }
)
public class RoutineExercise {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long routineId;
    private long exerciseId;
    private int sortOrder;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRoutineId() {
        return routineId;
    }

    public void setRoutineId(long routineId) {
        this.routineId = routineId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
