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
}
