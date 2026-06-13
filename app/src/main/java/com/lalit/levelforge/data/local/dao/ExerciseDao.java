package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.model.MuscleGroup;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY name")
    LiveData<List<Exercise>> observeExercises();

    @Query("SELECT * FROM exercises WHERE primaryMuscleGroup = :muscleGroup ORDER BY name")
    LiveData<List<Exercise>> observeByPrimaryMuscle(MuscleGroup muscleGroup);

    @Query("SELECT COUNT(*) FROM exercises")
    int countExercises();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertDefaults(List<Exercise> exercises);

    @Upsert
    void upsert(Exercise exercise);
}
