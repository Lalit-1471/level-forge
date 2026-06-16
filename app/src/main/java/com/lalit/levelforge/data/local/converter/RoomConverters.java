package com.lalit.levelforge.data.local.converter;

import com.lalit.levelforge.data.model.ActivityLevel;
import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.data.model.ExpSourceType;
import com.lalit.levelforge.data.model.Gender;
import com.lalit.levelforge.data.model.GoalType;
import com.lalit.levelforge.data.model.MuscleGroup;
import com.lalit.levelforge.data.model.ProgressionEventType;
import com.lalit.levelforge.data.model.QuestMetricType;
import com.lalit.levelforge.data.model.QuestResetType;
import com.lalit.levelforge.data.model.QuestRewardType;
import com.lalit.levelforge.data.model.RankTier;
import com.lalit.levelforge.data.model.SetType;

import androidx.room.TypeConverter;

public final class RoomConverters {

    private RoomConverters() {
    }

    @TypeConverter
    public static Gender toGender(String value) {
        return value == null ? null : Gender.valueOf(value);
    }

    @TypeConverter
    public static String fromGender(Gender value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static GoalType toGoalType(String value) {
        return value == null ? null : GoalType.valueOf(value);
    }

    @TypeConverter
    public static String fromGoalType(GoalType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static ActivityLevel toActivityLevel(String value) {
        return value == null ? null : ActivityLevel.valueOf(value);
    }

    @TypeConverter
    public static String fromActivityLevel(ActivityLevel value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static ExerciseType toExerciseType(String value) {
        return value == null ? null : ExerciseType.valueOf(value);
    }

    @TypeConverter
    public static String fromExerciseType(ExerciseType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static MuscleGroup toMuscleGroup(String value) {
        return value == null ? null : MuscleGroup.valueOf(value);
    }

    @TypeConverter
    public static String fromMuscleGroup(MuscleGroup value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static SetType toSetType(String value) {
        return value == null ? null : SetType.valueOf(value);
    }

    @TypeConverter
    public static String fromSetType(SetType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static RankTier toRankTier(String value) {
        return value == null ? null : RankTier.valueOf(value);
    }

    @TypeConverter
    public static String fromRankTier(RankTier value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static ExpSourceType toExpSourceType(String value) {
        return value == null ? null : ExpSourceType.valueOf(value);
    }

    @TypeConverter
    public static String fromExpSourceType(ExpSourceType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static ProgressionEventType toProgressionEventType(String value) {
        return value == null ? null : ProgressionEventType.valueOf(value);
    }

    @TypeConverter
    public static String fromProgressionEventType(ProgressionEventType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static QuestResetType toQuestResetType(String value) {
        return value == null ? null : QuestResetType.valueOf(value);
    }

    @TypeConverter
    public static String fromQuestResetType(QuestResetType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static QuestMetricType toQuestMetricType(String value) {
        return value == null ? null : QuestMetricType.valueOf(value);
    }

    @TypeConverter
    public static String fromQuestMetricType(QuestMetricType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static QuestRewardType toQuestRewardType(String value) {
        return value == null ? null : QuestRewardType.valueOf(value);
    }

    @TypeConverter
    public static String fromQuestRewardType(QuestRewardType value) {
        return value == null ? null : value.name();
    }
}
