package com.lalit.levelforge.ui.stats;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.local.relation.WorkoutSetWithExercise;
import com.lalit.levelforge.data.repo.ExerciseRepository;
import com.lalit.levelforge.data.repo.WorkoutRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatsViewModel extends ViewModel {

    private final LiveData<List<WorkoutSession>> completedSessions;
    private final LiveData<List<WorkoutSetWithExercise>> completedSetDetails;

    @Inject
    public StatsViewModel(WorkoutRepository workoutRepository,
                          ExerciseRepository exerciseRepository) {
        exerciseRepository.seedDefaultExercises();
        completedSessions = workoutRepository.observeCompletedSessions();
        completedSetDetails = workoutRepository.observeCompletedSetDetails();
    }

    public LiveData<List<WorkoutSession>> getCompletedSessions() {
        return completedSessions;
    }

    public LiveData<List<WorkoutSetWithExercise>> getCompletedSetDetails() {
        return completedSetDetails;
    }
}
