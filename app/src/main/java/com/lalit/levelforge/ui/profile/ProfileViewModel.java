package com.lalit.levelforge.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.LevelState;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.local.relation.WorkoutSetWithExercise;
import com.lalit.levelforge.data.repo.ProgressionRepository;
import com.lalit.levelforge.data.repo.WorkoutRepository;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final LiveData<List<WorkoutSession>> completedSessions;
    private final LiveData<LevelState> levelState;
    private final LiveData<List<WorkoutSetWithExercise>> completedSetDetails;
    private final MutableLiveData<Long> selectedSessionId = new MutableLiveData<>();
    private final LiveData<List<WorkoutSetWithExercise>> selectedWorkoutSets;

    @Inject
    public ProfileViewModel(WorkoutRepository workoutRepository,
                            ProgressionRepository progressionRepository) {
        progressionRepository.initializeLevelStateIfNeeded();
        completedSessions = workoutRepository.observeCompletedSessions();
        completedSetDetails = workoutRepository.observeCompletedSetDetails();
        levelState = progressionRepository.observeLevelState();
        selectedWorkoutSets = Transformations.switchMap(selectedSessionId, sessionId -> {
            if (sessionId == null || sessionId <= 0) {
                MutableLiveData<List<WorkoutSetWithExercise>> emptyDetails = new MutableLiveData<>();
                emptyDetails.setValue(Collections.emptyList());
                return emptyDetails;
            }
            return workoutRepository.observeSetDetailsForSession(sessionId);
        });
    }

    public LiveData<List<WorkoutSession>> getCompletedSessions() {
        return completedSessions;
    }

    public LiveData<LevelState> getLevelState() {
        return levelState;
    }

    public LiveData<List<WorkoutSetWithExercise>> getCompletedSetDetails() {
        return completedSetDetails;
    }

    public LiveData<List<WorkoutSetWithExercise>> getSelectedWorkoutSets() {
        return selectedWorkoutSets;
    }

    public void selectSession(long sessionId) {
        Long currentSessionId = selectedSessionId.getValue();
        if (currentSessionId != null && currentSessionId == sessionId) {
            selectedSessionId.setValue(0L);
        } else {
            selectedSessionId.setValue(sessionId);
        }
    }
}
