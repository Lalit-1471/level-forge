package com.lalit.levelforge.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.lalit.levelforge.data.local.dao.WorkoutSessionDao;
import com.lalit.levelforge.data.local.entity.WorkoutSession;

@Database(entities = {WorkoutSession.class}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WorkoutSessionDao workoutSessionDao();
}

