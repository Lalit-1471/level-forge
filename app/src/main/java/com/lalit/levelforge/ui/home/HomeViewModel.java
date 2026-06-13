package com.lalit.levelforge.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.LevelState;
import com.lalit.levelforge.data.local.entity.UserProfile;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.repo.ExerciseRepository;
import com.lalit.levelforge.data.repo.ProgressionRepository;
import com.lalit.levelforge.data.repo.UserProfileRepository;
import com.lalit.levelforge.data.repo.WorkoutRepository;
import com.lalit.levelforge.domain.progression.LevelCurve;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final MediatorLiveData<String> weeklySummary = new MediatorLiveData<>();
    private final MediatorLiveData<String> dailyTask = new MediatorLiveData<>();
    private final MediatorLiveData<String> subtitle = new MediatorLiveData<>();
    private final MediatorLiveData<String> levelValue = new MediatorLiveData<>();
    private final MediatorLiveData<String> expValue = new MediatorLiveData<>();
    private final MediatorLiveData<Boolean> onboardingComplete = new MediatorLiveData<>();

    @Inject
    public HomeViewModel(WorkoutRepository workoutRepository,
                         UserProfileRepository userProfileRepository,
                         ProgressionRepository progressionRepository,
                         ExerciseRepository exerciseRepository) {
        progressionRepository.initializeLevelStateIfNeeded();
        exerciseRepository.seedDefaultExercises();

        LiveData<List<WorkoutSession>> recentSessions = workoutRepository.getRecentSessions();
        LiveData<UserProfile> profile = userProfileRepository.observeProfile();
        LiveData<LevelState> levelState = progressionRepository.observeLevelState();

        weeklySummary.setValue("No sessions logged yet");
        dailyTask.setValue("Complete 1 strength workout and 1 mobility block");
        subtitle.setValue("Start your awakening, then forge workouts into EXP.");
        levelValue.setValue("Level 1 Novice Hunter");
        expValue.setValue("0 / 120 EXP");
        onboardingComplete.setValue(false);

        subtitle.addSource(profile, userProfile -> {
            boolean complete = userProfile != null && userProfile.isOnboardingComplete();
            onboardingComplete.setValue(complete);
            if (complete) {
                subtitle.setValue("Profile ready. Log workouts, earn EXP, and climb ranks.");
                dailyTask.setValue("Complete 1 strength workout and review your exercise library");
            } else {
                subtitle.setValue("Start your awakening, then forge workouts into EXP.");
                dailyTask.setValue("Complete onboarding to unlock daily quests");
            }
        });

        levelValue.addSource(levelState, state -> {
            if (state == null) {
                levelValue.setValue("Level 1 Novice Hunter");
                return;
            }
            levelValue.setValue("Level " + state.getLevel() + " " + state.getActiveTitle());
        });

        expValue.addSource(levelState, state -> {
            int totalExp = state == null ? 0 : state.getTotalExp();
            expValue.setValue(LevelCurve.expIntoCurrentLevel(totalExp) + " / "
                    + LevelCurve.expToAdvanceFromLevel(LevelCurve.levelForTotalExp(totalExp)) + " EXP");
        });

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

    public LiveData<String> getSubtitle() {
        return subtitle;
    }

    public LiveData<String> getLevelValue() {
        return levelValue;
    }

    public LiveData<String> getExpValue() {
        return expValue;
    }

    public LiveData<Boolean> isOnboardingComplete() {
        return onboardingComplete;
    }
}
