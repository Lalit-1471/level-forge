package com.lalit.levelforge.di;

import android.content.Context;

import androidx.room.Room;

import com.lalit.levelforge.data.local.AppDatabase;
import com.lalit.levelforge.data.local.dao.ExerciseDao;
import com.lalit.levelforge.data.local.dao.ExpEventDao;
import com.lalit.levelforge.data.local.dao.LevelStateDao;
import com.lalit.levelforge.data.local.dao.UserProfileDao;
import com.lalit.levelforge.data.local.dao.WorkoutSessionDao;
import com.lalit.levelforge.data.local.dao.WorkoutSetDao;

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

    @Provides
    static WorkoutSetDao provideWorkoutSetDao(AppDatabase appDatabase) {
        return appDatabase.workoutSetDao();
    }

    @Provides
    static ExerciseDao provideExerciseDao(AppDatabase appDatabase) {
        return appDatabase.exerciseDao();
    }

    @Provides
    static ExpEventDao provideExpEventDao(AppDatabase appDatabase) {
        return appDatabase.expEventDao();
    }

    @Provides
    static LevelStateDao provideLevelStateDao(AppDatabase appDatabase) {
        return appDatabase.levelStateDao();
    }

    @Provides
    static UserProfileDao provideUserProfileDao(AppDatabase appDatabase) {
        return appDatabase.userProfileDao();
    }
}
