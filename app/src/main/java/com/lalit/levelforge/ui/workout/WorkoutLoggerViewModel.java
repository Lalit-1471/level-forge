package com.lalit.levelforge.ui.workout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.model.SetType;
import com.lalit.levelforge.data.repo.ExerciseRepository;
import com.lalit.levelforge.data.repo.WorkoutLogRepository;
import com.lalit.levelforge.domain.progression.ExpCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class WorkoutLoggerViewModel extends ViewModel {

    private final WorkoutLogRepository workoutLogRepository;
    private final LiveData<List<Exercise>> exercises;
    private final MutableLiveData<List<LoggedExercise>> loggedExercises = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> totalExp = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> reviewMode = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);
    private final long startedAtMillis = System.currentTimeMillis();

    @Inject
    public WorkoutLoggerViewModel(ExerciseRepository exerciseRepository,
                                  WorkoutLogRepository workoutLogRepository) {
        this.workoutLogRepository = workoutLogRepository;
        exerciseRepository.seedDefaultExercises();
        exercises = exerciseRepository.observeExercises();
    }

    public LiveData<List<Exercise>> getExercises() {
        return exercises;
    }

    public LiveData<List<LoggedExercise>> getLoggedExercises() {
        return loggedExercises;
    }

    public LiveData<Integer> getTotalExp() {
        return totalExp;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public LiveData<Boolean> getReviewMode() {
        return reviewMode;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public void addExercise(Exercise exercise) {
        if (exercise == null || containsExercise(exercise.getId())) {
            return;
        }
        List<LoggedExercise> current = new ArrayList<>(safeLoggedExercises());
        current.add(new LoggedExercise(exercise));
        loggedExercises.setValue(Collections.unmodifiableList(current));
    }

    public void addSet(long exerciseId, SetType setType, int reps, double weightKg,
                       int durationSeconds, double distanceMeters, double assistanceKg,
                       boolean progressiveOverload) {
        List<LoggedExercise> updatedExercises = new ArrayList<>();
        for (LoggedExercise loggedExercise : safeLoggedExercises()) {
            if (loggedExercise.getExercise().getId() != exerciseId) {
                updatedExercises.add(loggedExercise);
                continue;
            }

            List<LoggedSet> updatedSets = new ArrayList<>(loggedExercise.getSets());
            WorkoutSet workoutSet = new WorkoutSet();
            workoutSet.setExerciseId(exerciseId);
            workoutSet.setSetNumber(updatedSets.size() + 1);
            workoutSet.setSetType(setType);
            workoutSet.setReps(reps);
            workoutSet.setWeightKg(weightKg);
            workoutSet.setDurationSeconds(durationSeconds);
            workoutSet.setDistanceMeters(distanceMeters);
            workoutSet.setAssistanceKg(assistanceKg);
            workoutSet.setCompleted(true);

            int exp = ExpCalculator.expForSet(loggedExercise.getExercise(), workoutSet, progressiveOverload);
            updatedSets.add(new LoggedSet(loggedExercise.getExercise(), workoutSet, exp));
            updatedExercises.add(new LoggedExercise(loggedExercise.getExercise(), updatedSets));
        }
        loggedExercises.setValue(Collections.unmodifiableList(updatedExercises));
        totalExp.setValue(sumExp(updatedExercises));
    }

    public void replaceSet(long exerciseId, int setIndex, SetType setType, int reps, double weightKg,
                           int durationSeconds, double distanceMeters, double assistanceKg,
                           boolean progressiveOverload) {
        List<LoggedExercise> updatedExercises = new ArrayList<>();
        for (LoggedExercise loggedExercise : safeLoggedExercises()) {
            if (loggedExercise.getExercise().getId() != exerciseId) {
                updatedExercises.add(loggedExercise);
                continue;
            }

            List<LoggedSet> updatedSets = new ArrayList<>(loggedExercise.getSets());
            if (setIndex < 0 || setIndex >= updatedSets.size()) {
                updatedExercises.add(loggedExercise);
                continue;
            }

            WorkoutSet workoutSet = updatedSets.get(setIndex).getWorkoutSet();
            workoutSet.setSetType(setType);
            workoutSet.setReps(reps);
            workoutSet.setWeightKg(weightKg);
            workoutSet.setDurationSeconds(durationSeconds);
            workoutSet.setDistanceMeters(distanceMeters);
            workoutSet.setAssistanceKg(assistanceKg);
            workoutSet.setCompleted(true);

            int exp = ExpCalculator.expForSet(loggedExercise.getExercise(), workoutSet, progressiveOverload);
            updatedSets.set(setIndex, new LoggedSet(loggedExercise.getExercise(), workoutSet, exp));
            updatedExercises.add(new LoggedExercise(loggedExercise.getExercise(), updatedSets));
        }
        loggedExercises.setValue(Collections.unmodifiableList(updatedExercises));
        totalExp.setValue(sumExp(updatedExercises));
    }

    public void removeSet(long exerciseId, int setIndex) {
        List<LoggedExercise> updatedExercises = new ArrayList<>();
        for (LoggedExercise loggedExercise : safeLoggedExercises()) {
            if (loggedExercise.getExercise().getId() != exerciseId) {
                updatedExercises.add(loggedExercise);
                continue;
            }
            List<LoggedSet> updatedSets = new ArrayList<>(loggedExercise.getSets());
            if (setIndex >= 0 && setIndex < updatedSets.size()) {
                updatedSets.remove(setIndex);
            }
            updatedExercises.add(new LoggedExercise(loggedExercise.getExercise(), renumberSets(updatedSets)));
        }
        loggedExercises.setValue(Collections.unmodifiableList(updatedExercises));
        totalExp.setValue(sumExp(updatedExercises));
    }

    public void finishWorkout() {
        if (setCount() > 0) {
            reviewMode.setValue(true);
        }
    }

    public void postWorkout(String title, int durationSeconds) {
        List<LoggedExercise> currentExercises = safeLoggedExercises();
        List<WorkoutSet> sets = new ArrayList<>();
        for (LoggedExercise loggedExercise : currentExercises) {
            for (LoggedSet loggedSet : loggedExercise.getSets()) {
                sets.add(loggedSet.getWorkoutSet());
            }
        }
        if (sets.isEmpty()) {
            return;
        }

        int exp = sumExp(currentExercises);
        workoutLogRepository.saveCompletedWorkout(title, sets, exp, durationSeconds, (sessionId, sessionExp) -> {
            loggedExercises.setValue(Collections.emptyList());
            totalExp.setValue(0);
            reviewMode.setValue(false);
            saved.setValue(true);
        });
    }

    public void discardWorkout() {
        loggedExercises.setValue(Collections.emptyList());
        totalExp.setValue(0);
        reviewMode.setValue(false);
    }

    public void consumeSaved() {
        saved.setValue(false);
    }

    public int setCount() {
        int count = 0;
        for (LoggedExercise loggedExercise : safeLoggedExercises()) {
            count += loggedExercise.getSets().size();
        }
        return count;
    }

    private boolean containsExercise(long exerciseId) {
        for (LoggedExercise loggedExercise : safeLoggedExercises()) {
            if (loggedExercise.getExercise().getId() == exerciseId) {
                return true;
            }
        }
        return false;
    }

    private List<LoggedExercise> safeLoggedExercises() {
        List<LoggedExercise> value = loggedExercises.getValue();
        return value == null ? Collections.emptyList() : value;
    }

    private List<LoggedSet> renumberSets(List<LoggedSet> sets) {
        List<LoggedSet> renumbered = new ArrayList<>();
        int setNumber = 1;
        for (LoggedSet loggedSet : sets) {
            loggedSet.getWorkoutSet().setSetNumber(setNumber);
            renumbered.add(loggedSet);
            setNumber++;
        }
        return renumbered;
    }

    private int sumExp(List<LoggedExercise> exercises) {
        int sum = 0;
        for (LoggedExercise loggedExercise : exercises) {
            for (LoggedSet set : loggedExercise.getSets()) {
                sum += set.getExp();
            }
        }
        return sum;
    }
}
