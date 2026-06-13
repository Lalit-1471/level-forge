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
    private final MutableLiveData<List<LoggedSet>> loggedSets = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> totalExp = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);

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

    public LiveData<List<LoggedSet>> getLoggedSets() {
        return loggedSets;
    }

    public LiveData<Integer> getTotalExp() {
        return totalExp;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public void addSet(Exercise exercise, SetType setType, int reps, double weightKg,
                       int durationSeconds, double distanceMeters, double assistanceKg,
                       boolean progressiveOverload) {
        if (exercise == null) {
            return;
        }

        List<LoggedSet> currentSets = new ArrayList<>(safeLoggedSets());
        WorkoutSet workoutSet = new WorkoutSet();
        workoutSet.setExerciseId(exercise.getId());
        workoutSet.setSetNumber(currentSets.size() + 1);
        workoutSet.setSetType(setType);
        workoutSet.setReps(reps);
        workoutSet.setWeightKg(weightKg);
        workoutSet.setDurationSeconds(durationSeconds);
        workoutSet.setDistanceMeters(distanceMeters);
        workoutSet.setAssistanceKg(assistanceKg);
        workoutSet.setCompleted(true);

        int exp = ExpCalculator.expForSet(exercise, workoutSet, progressiveOverload);
        currentSets.add(new LoggedSet(exercise, workoutSet, exp));
        loggedSets.setValue(Collections.unmodifiableList(currentSets));
        totalExp.setValue(sumExp(currentSets));
    }

    public void finishWorkout(String title) {
        List<LoggedSet> currentSets = safeLoggedSets();
        if (currentSets.isEmpty()) {
            return;
        }

        List<WorkoutSet> sets = new ArrayList<>();
        for (LoggedSet loggedSet : currentSets) {
            sets.add(loggedSet.getWorkoutSet());
        }
        int exp = sumExp(currentSets);
        workoutLogRepository.saveCompletedWorkout(title, sets, exp, (sessionId, sessionExp) -> {
            loggedSets.setValue(Collections.emptyList());
            totalExp.setValue(0);
            saved.setValue(true);
        });
    }

    public void consumeSaved() {
        saved.setValue(false);
    }

    private List<LoggedSet> safeLoggedSets() {
        List<LoggedSet> value = loggedSets.getValue();
        return value == null ? Collections.emptyList() : value;
    }

    private int sumExp(List<LoggedSet> sets) {
        int sum = 0;
        for (LoggedSet set : sets) {
            sum += set.getExp();
        }
        return sum;
    }
}
