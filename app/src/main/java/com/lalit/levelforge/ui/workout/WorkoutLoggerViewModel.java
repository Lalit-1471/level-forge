package com.lalit.levelforge.ui.workout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.data.model.SetType;
import com.lalit.levelforge.data.repo.ExerciseRepository;
import com.lalit.levelforge.data.repo.WorkoutLogRepository;
import com.lalit.levelforge.domain.progression.ExpCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<Long, Double> historicalBestEffortByExercise = new HashMap<>();
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
        loadHistoricalBestEffort(exercise);
    }

    public void addSet(long exerciseId, SetType setType, int reps, double weightKg,
                       int durationSeconds, double distanceMeters, double assistanceKg) {
        List<LoggedExercise> updatedExercises = new ArrayList<>();
        for (LoggedExercise loggedExercise : safeLoggedExercises()) {
            if (loggedExercise.getExercise().getId() != exerciseId) {
                updatedExercises.add(loggedExercise);
                continue;
            }

            List<WorkoutSet> updatedSets = copyWorkoutSets(loggedExercise.getSets());
            updatedSets.add(buildWorkoutSet(
                    exerciseId,
                    updatedSets.size() + 1,
                    setType,
                    reps,
                    weightKg,
                    durationSeconds,
                    distanceMeters,
                    assistanceKg
            ));
            updatedExercises.add(new LoggedExercise(
                    loggedExercise.getExercise(),
                    rebuildLoggedSets(loggedExercise.getExercise(), updatedSets)
            ));
        }
        loggedExercises.setValue(Collections.unmodifiableList(updatedExercises));
        totalExp.setValue(sumExp(updatedExercises));
    }

    public void replaceSet(long exerciseId, int setIndex, SetType setType, int reps, double weightKg,
                           int durationSeconds, double distanceMeters, double assistanceKg) {
        List<LoggedExercise> updatedExercises = new ArrayList<>();
        for (LoggedExercise loggedExercise : safeLoggedExercises()) {
            if (loggedExercise.getExercise().getId() != exerciseId) {
                updatedExercises.add(loggedExercise);
                continue;
            }

            List<WorkoutSet> updatedSets = copyWorkoutSets(loggedExercise.getSets());
            if (setIndex < 0 || setIndex >= updatedSets.size()) {
                updatedExercises.add(loggedExercise);
                continue;
            }

            updatedSets.set(setIndex, buildWorkoutSet(
                    exerciseId,
                    setIndex + 1,
                    setType,
                    reps,
                    weightKg,
                    durationSeconds,
                    distanceMeters,
                    assistanceKg
            ));
            updatedExercises.add(new LoggedExercise(
                    loggedExercise.getExercise(),
                    rebuildLoggedSets(loggedExercise.getExercise(), updatedSets)
            ));
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
            List<WorkoutSet> updatedSets = copyWorkoutSets(loggedExercise.getSets());
            if (setIndex >= 0 && setIndex < updatedSets.size()) {
                updatedSets.remove(setIndex);
            }
            updatedExercises.add(new LoggedExercise(
                    loggedExercise.getExercise(),
                    rebuildLoggedSets(loggedExercise.getExercise(), updatedSets)
            ));
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

    private WorkoutSet buildWorkoutSet(long exerciseId, int setNumber, SetType setType, int reps, double weightKg,
                                       int durationSeconds, double distanceMeters, double assistanceKg) {
        WorkoutSet workoutSet = new WorkoutSet();
        workoutSet.setExerciseId(exerciseId);
        workoutSet.setSetNumber(setNumber);
        workoutSet.setSetType(setType);
        workoutSet.setReps(reps);
        workoutSet.setWeightKg(weightKg);
        workoutSet.setDurationSeconds(durationSeconds);
        workoutSet.setDistanceMeters(distanceMeters);
        workoutSet.setAssistanceKg(assistanceKg);
        workoutSet.setCompleted(true);
        return workoutSet;
    }

    private List<WorkoutSet> copyWorkoutSets(List<LoggedSet> sets) {
        List<WorkoutSet> copied = new ArrayList<>();
        for (LoggedSet loggedSet : sets) {
            WorkoutSet source = loggedSet.getWorkoutSet();
            copied.add(buildWorkoutSet(
                    source.getExerciseId(),
                    source.getSetNumber(),
                    source.getSetType(),
                    source.getReps(),
                    source.getWeightKg(),
                    source.getDurationSeconds(),
                    source.getDistanceMeters(),
                    source.getAssistanceKg()
            ));
        }
        return copied;
    }

    private List<LoggedSet> rebuildLoggedSets(Exercise exercise, List<WorkoutSet> workoutSets) {
        List<LoggedSet> rebuiltSets = new ArrayList<>();
        int setNumber = 1;
        for (WorkoutSet workoutSet : workoutSets) {
            workoutSet.setSetNumber(setNumber);
            boolean progressiveOverload = isProgressiveOverload(exercise, workoutSet, rebuiltSets);
            int exp = ExpCalculator.expForSet(exercise, workoutSet, progressiveOverload);
            rebuiltSets.add(new LoggedSet(exercise, workoutSet, exp));
            setNumber++;
        }
        return rebuiltSets;
    }

    private boolean isProgressiveOverload(Exercise exercise, WorkoutSet candidate, List<LoggedSet> previousSets) {
        if (candidate.getSetType() == SetType.WARMUP) {
            return false;
        }

        ExerciseType exerciseType = exercise.getExerciseType();
        double candidateScore = effortScore(exerciseType, candidate);
        if (candidateScore <= 0) {
            return false;
        }

        double bestPreviousScore = historicalBestEffortByExercise.containsKey(exercise.getId())
                ? historicalBestEffortByExercise.get(exercise.getId())
                : 0;
        for (LoggedSet previousSet : previousSets) {
            if (previousSet.getWorkoutSet().getSetType() == SetType.WARMUP) {
                continue;
            }
            bestPreviousScore = Math.max(
                    bestPreviousScore,
                    effortScore(exerciseType, previousSet.getWorkoutSet())
            );
        }
        return bestPreviousScore > 0 && candidateScore > bestPreviousScore;
    }

    private void loadHistoricalBestEffort(Exercise exercise) {
        if (historicalBestEffortByExercise.containsKey(exercise.getId())) {
            return;
        }
        workoutLogRepository.getCompletedSetsForExercise(exercise.getId(), (exerciseId, sets) -> {
            double bestScore = 0;
            for (WorkoutSet workoutSet : sets) {
                if (workoutSet.getSetType() == SetType.WARMUP) {
                    continue;
                }
                bestScore = Math.max(bestScore, effortScore(exercise.getExerciseType(), workoutSet));
            }
            historicalBestEffortByExercise.put(exerciseId, bestScore);
            List<LoggedExercise> rebuiltExercises = rebuildExerciseExp(safeLoggedExercises());
            loggedExercises.setValue(rebuiltExercises);
            totalExp.setValue(sumExp(rebuiltExercises));
        });
    }

    private List<LoggedExercise> rebuildExerciseExp(List<LoggedExercise> exercises) {
        List<LoggedExercise> rebuiltExercises = new ArrayList<>();
        for (LoggedExercise loggedExercise : exercises) {
            rebuiltExercises.add(new LoggedExercise(
                    loggedExercise.getExercise(),
                    rebuildLoggedSets(
                            loggedExercise.getExercise(),
                            copyWorkoutSets(loggedExercise.getSets())
                    )
            ));
        }
        return Collections.unmodifiableList(rebuiltExercises);
    }

    private double effortScore(ExerciseType exerciseType, WorkoutSet workoutSet) {
        if (exerciseType == null) {
            return workoutSet.getReps()
                    + workoutSet.getWeightKg()
                    + workoutSet.getDurationSeconds()
                    + workoutSet.getDistanceMeters();
        }

        switch (exerciseType) {
            case WEIGHT_REPS:
            case WEIGHTED_BODYWEIGHT:
                return workoutSet.getWeightKg() * workoutSet.getReps();
            case ASSISTED_BODYWEIGHT:
                return workoutSet.getReps() * 100.0 - workoutSet.getAssistanceKg();
            case BODYWEIGHT_REPS:
                return workoutSet.getReps();
            case DURATION:
                return workoutSet.getDurationSeconds();
            case WEIGHT_DURATION:
                return workoutSet.getWeightKg() * workoutSet.getDurationSeconds();
            case DISTANCE_DURATION:
                return workoutSet.getDistanceMeters() + workoutSet.getDurationSeconds() / 10.0;
            case WEIGHT_DISTANCE:
                return workoutSet.getWeightKg() * workoutSet.getDistanceMeters();
            default:
                return 0;
        }
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
