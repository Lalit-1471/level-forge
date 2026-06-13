package com.lalit.levelforge.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.repo.WorkoutRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final LiveData<List<WorkoutSession>> completedSessions;

    @Inject
    public ProfileViewModel(WorkoutRepository workoutRepository) {
        completedSessions = workoutRepository.observeCompletedSessions();
    }

    public LiveData<List<WorkoutSession>> getCompletedSessions() {
        return completedSessions;
    }
}
