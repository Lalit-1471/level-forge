package com.lalit.levelforge.data.repo;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.ExerciseDao;
import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.model.MuscleGroup;
import com.lalit.levelforge.data.seed.DefaultExercises;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExerciseRepository {

    private final ExerciseDao exerciseDao;
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public ExerciseRepository(ExerciseDao exerciseDao) {
        this.exerciseDao = exerciseDao;
    }

    public LiveData<List<Exercise>> observeExercises() {
        return exerciseDao.observeExercises();
    }

    public LiveData<List<Exercise>> observeByPrimaryMuscle(MuscleGroup muscleGroup) {
        return exerciseDao.observeByPrimaryMuscle(muscleGroup);
    }

    public void seedDefaultExercises() {
        diskExecutor.execute(() -> exerciseDao.insertDefaults(DefaultExercises.create()));
    }

    public void saveExercise(Exercise exercise) {
        diskExecutor.execute(() -> exerciseDao.upsert(exercise));
    }
}
