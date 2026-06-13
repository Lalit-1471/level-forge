package com.lalit.levelforge.di;

import android.content.Context;

import androidx.room.Room;

import com.lalit.levelforge.data.local.AppDatabase;
import com.lalit.levelforge.data.local.dao.WorkoutSessionDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class AppModule {

    private AppModule() {
    }

    @Provides
    @Singleton
    static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "levelforge.db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    static WorkoutSessionDao provideWorkoutSessionDao(AppDatabase appDatabase) {
        return appDatabase.workoutSessionDao();
    }
}

