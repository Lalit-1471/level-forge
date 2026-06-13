package com.lalit.levelforge.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.repo.WorkoutRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final MediatorLiveData<String> weeklySummary = new MediatorLiveData<>();
    private final MediatorLiveData<String> dailyTask = new MediatorLiveData<>();

    @Inject
    public HomeViewModel(WorkoutRepository workoutRepository) {
        LiveData<List<WorkoutSession>> recentSessions = workoutRepository.getRecentSessions();
        weeklySummary.setValue("No sessions logged yet");
        dailyTask.setValue("Complete 1 strength workout and 1 mobility block");
        weeklySummary.addSource(recentSessions, sessions -> {
            int count = sessions == null ? 0 : sessions.size();
            if (count == 0) {
                weeklySummary.setValue("No sessions logged yet");
            } else {
                weeklySummary.setValue(count + " recent workout runs tracked");
            }
        });
    }

    public LiveData<String> getWeeklySummary() {
        return weeklySummary;
    }

    public LiveData<String> getDailyTask() {
        return dailyTask;
    }
}

