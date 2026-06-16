package com.lalit.levelforge.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lalit.levelforge.data.local.converter.RoomConverters;
import com.lalit.levelforge.data.local.dao.ExerciseDao;
import com.lalit.levelforge.data.local.dao.ExpEventDao;
import com.lalit.levelforge.data.local.dao.LevelStateDao;
import com.lalit.levelforge.data.local.dao.ProgressionEventDao;
import com.lalit.levelforge.data.local.dao.QuestDefinitionDao;
import com.lalit.levelforge.data.local.dao.QuestProgressDao;
import com.lalit.levelforge.data.local.dao.UserProfileDao;
import com.lalit.levelforge.data.local.dao.WorkoutSessionDao;
import com.lalit.levelforge.data.local.dao.WorkoutSetDao;
import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.ExpEvent;
import com.lalit.levelforge.data.local.entity.LevelState;
import com.lalit.levelforge.data.local.entity.ProgressionEvent;
import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.local.entity.UserProfile;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.local.entity.WorkoutSet;

@Database(
        entities = {
                WorkoutSession.class,
                WorkoutSet.class,
                Exercise.class,
                ExpEvent.class,
                ProgressionEvent.class,
                QuestDefinition.class,
                QuestProgress.class,
                LevelState.class,
                UserProfile.class
        },
        version = 3,
        exportSchema = true
)
@TypeConverters(RoomConverters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WorkoutSessionDao workoutSessionDao();

    public abstract WorkoutSetDao workoutSetDao();

    public abstract ExerciseDao exerciseDao();

    public abstract ExpEventDao expEventDao();

    public abstract ProgressionEventDao progressionEventDao();

    public abstract QuestDefinitionDao questDefinitionDao();

    public abstract QuestProgressDao questProgressDao();

    public abstract LevelStateDao levelStateDao();

    public abstract UserProfileDao userProfileDao();
}
