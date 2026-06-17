package com.lalit.levelforge.data.local;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class DatabaseMigrations {

    private DatabaseMigrations() {
    }

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `streak_state` ("
                    + "`id` INTEGER NOT NULL, "
                    + "`currentStreakDays` INTEGER NOT NULL, "
                    + "`longestStreakDays` INTEGER NOT NULL, "
                    + "`streakShields` INTEGER NOT NULL, "
                    + "`lastLoginDayStartMillis` INTEGER NOT NULL, "
                    + "`updatedAt` INTEGER NOT NULL, "
                    + "PRIMARY KEY(`id`))");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `quest_definitions` ADD COLUMN `rarity` TEXT DEFAULT 'COMMON'");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `quest_objectives` ("
                    + "`id` TEXT NOT NULL, "
                    + "`questId` TEXT, "
                    + "`label` TEXT, "
                    + "`metricType` TEXT, "
                    + "`targetCount` INTEGER NOT NULL, "
                    + "`sortOrder` INTEGER NOT NULL, "
                    + "PRIMARY KEY(`id`))");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_quest_objectives_questId` "
                    + "ON `quest_objectives` (`questId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_quest_objectives_sortOrder` "
                    + "ON `quest_objectives` (`sortOrder`)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `quest_objective_progress` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`objectiveId` TEXT, "
                    + "`questId` TEXT, "
                    + "`periodStartMillis` INTEGER NOT NULL, "
                    + "`progressCount` INTEGER NOT NULL, "
                    + "`completed` INTEGER NOT NULL, "
                    + "`updatedAt` INTEGER NOT NULL)");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS "
                    + "`index_quest_objective_progress_objectiveId_periodStartMillis` "
                    + "ON `quest_objective_progress` (`objectiveId`, `periodStartMillis`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_quest_objective_progress_questId` "
                    + "ON `quest_objective_progress` (`questId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_quest_objective_progress_periodStartMillis` "
                    + "ON `quest_objective_progress` (`periodStartMillis`)");
        }
    };
}
