package com.lalit.levelforge.data.repo;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.ExerciseDao;
import com.lalit.levelforge.data.local.dao.RoutineDao;
import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.Routine;
import com.lalit.levelforge.data.local.entity.RoutineExercise;
import com.lalit.levelforge.data.local.entity.RoutineSet;
import com.lalit.levelforge.data.local.entity.WorkoutSet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RoutineRepository {

    public interface SaveCallback {
        void onSaved(long routineId);
    }

    public interface LoadCallback {
        void onLoaded(List<RoutineExerciseTemplate> exercises);
    }

    public static class RoutineExerciseTemplate {
        private final Exercise exercise;
        private final List<WorkoutSet> sets;

        public RoutineExerciseTemplate(Exercise exercise, List<WorkoutSet> sets) {
            this.exercise = exercise;
            this.sets = sets;
        }

        public Exercise getExercise() {
            return exercise;
        }

        public List<WorkoutSet> getSets() {
            return sets;
        }
    }

    private final RoutineDao routineDao;
    private final ExerciseDao exerciseDao;
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public RoutineRepository(RoutineDao routineDao, ExerciseDao exerciseDao) {
        this.routineDao = routineDao;
        this.exerciseDao = exerciseDao;
    }

    public LiveData<List<Routine>> observeRoutines() {
        return routineDao.observeRoutines();
    }

    public void saveRoutine(String title, String notes, List<WorkoutSet> sets, SaveCallback callback) {
        diskExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            Routine routine = new Routine();
            routine.setTitle(cleanTitle(title));
            routine.setNotes(notes == null ? "" : notes.trim());
            routine.setCreatedAt(now);
            routine.setUpdatedAt(now);

            long routineId = routineDao.insertRoutine(routine);
            long currentExerciseId = -1L;
            long routineExerciseId = -1L;
            int exerciseOrder = 0;
            int setNumber = 1;

            for (WorkoutSet workoutSet : sets) {
                if (workoutSet.getExerciseId() != currentExerciseId) {
                    currentExerciseId = workoutSet.getExerciseId();
                    exerciseOrder++;
                    setNumber = 1;

                    RoutineExercise routineExercise = new RoutineExercise();
                    routineExercise.setRoutineId(routineId);
                    routineExercise.setExerciseId(currentExerciseId);
                    routineExercise.setSortOrder(exerciseOrder);
                    routineExerciseId = routineDao.insertRoutineExercise(routineExercise);
                }

                RoutineSet routineSet = new RoutineSet();
                routineSet.setRoutineExerciseId(routineExerciseId);
                routineSet.setSetNumber(setNumber);
                routineSet.setSetType(workoutSet.getSetType());
                routineSet.setReps(workoutSet.getReps());
                routineSet.setWeightKg(workoutSet.getWeightKg());
                routineSet.setDurationSeconds(workoutSet.getDurationSeconds());
                routineSet.setDistanceMeters(workoutSet.getDistanceMeters());
                routineSet.setAssistanceKg(workoutSet.getAssistanceKg());
                routineDao.insertRoutineSet(routineSet);
                setNumber++;
            }

            if (callback != null) {
                mainHandler.post(() -> callback.onSaved(routineId));
            }
        });
    }

    public void loadRoutine(long routineId, LoadCallback callback) {
        diskExecutor.execute(() -> {
            List<RoutineExerciseTemplate> templates = new ArrayList<>();
            for (RoutineExercise routineExercise : routineDao.getRoutineExercises(routineId)) {
                Exercise exercise = exerciseDao.getExerciseById(routineExercise.getExerciseId());
                if (exercise == null) {
                    continue;
                }
                List<WorkoutSet> workoutSets = new ArrayList<>();
                for (RoutineSet routineSet : routineDao.getRoutineSets(routineExercise.getId())) {
                    workoutSets.add(toWorkoutSet(routineExercise.getExerciseId(), routineSet));
                }
                templates.add(new RoutineExerciseTemplate(exercise, workoutSets));
            }

            if (callback != null) {
                mainHandler.post(() -> callback.onLoaded(templates));
            }
        });
    }

    private WorkoutSet toWorkoutSet(long exerciseId, RoutineSet routineSet) {
        WorkoutSet workoutSet = new WorkoutSet();
        workoutSet.setExerciseId(exerciseId);
        workoutSet.setSetNumber(routineSet.getSetNumber());
        workoutSet.setSetType(routineSet.getSetType());
        workoutSet.setReps(routineSet.getReps());
        workoutSet.setWeightKg(routineSet.getWeightKg());
        workoutSet.setDurationSeconds(routineSet.getDurationSeconds());
        workoutSet.setDistanceMeters(routineSet.getDistanceMeters());
        workoutSet.setAssistanceKg(routineSet.getAssistanceKg());
        workoutSet.setCompleted(false);
        return workoutSet;
    }

    private String cleanTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Saved routine";
        }
        return title.trim();
    }
}
