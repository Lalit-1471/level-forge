package com.lalit.levelforge.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.LevelState;
import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.local.entity.UserProfile;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.repo.ExerciseRepository;
import com.lalit.levelforge.data.repo.ProgressionRepository;
import com.lalit.levelforge.data.repo.QuestRepository;
import com.lalit.levelforge.data.repo.UserProfileRepository;
import com.lalit.levelforge.data.repo.WorkoutRepository;
import com.lalit.levelforge.domain.calendar.TrainingCalendar;
import com.lalit.levelforge.domain.progression.LevelCurve;
import com.lalit.levelforge.domain.quest.QuestRotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final List<QuestDefinition> latestQuestDefinitions = new ArrayList<>();
    private final List<QuestProgress> latestDailyProgress = new ArrayList<>();
    private boolean latestProfileComplete;

    @Inject
    public HomeViewModel(WorkoutRepository workoutRepository,
                         UserProfileRepository userProfileRepository,
                         ProgressionRepository progressionRepository,
                         ExerciseRepository exerciseRepository,
                         QuestRepository questRepository) {
        progressionRepository.initializeLevelStateIfNeeded();
        exerciseRepository.seedDefaultExercises();
        questRepository.seedDefaultQuestDefinitions();
        questRepository.recordDailyLogin();

        LiveData<List<WorkoutSession>> recentSessions = workoutRepository.getRecentSessions();
        LiveData<UserProfile> profile = userProfileRepository.observeProfile();
        LiveData<LevelState> levelState = progressionRepository.observeLevelState();
        LiveData<List<QuestDefinition>> questDefinitions = questRepository.observeActiveDefinitions();
        LiveData<List<QuestProgress>> todayProgress = questRepository.observeProgressForPeriod(
                TrainingCalendar.startOfDay(System.currentTimeMillis())
        );

        weeklySummary.setValue("No sessions logged yet");
        dailyTask.setValue("Complete 1 strength workout and 1 mobility block");
        subtitle.setValue("Start your awakening, then forge workouts into EXP.");
        levelValue.setValue("Level 1 Novice Hunter");
        expValue.setValue("0 / 120 EXP");
        onboardingComplete.setValue(false);

        subtitle.addSource(profile, userProfile -> {
            latestProfileComplete = userProfile != null && userProfile.isOnboardingComplete();
            onboardingComplete.setValue(latestProfileComplete);
            if (latestProfileComplete) {
                subtitle.setValue("Profile ready. Log workouts, earn EXP, and climb ranks.");
            } else {
                subtitle.setValue("Start your awakening, then forge workouts into EXP.");
            }
            renderDailyQuestPreview();
        });

        dailyTask.addSource(questDefinitions, definitions -> {
            latestQuestDefinitions.clear();
            if (definitions != null) {
                latestQuestDefinitions.addAll(definitions);
            }
            renderDailyQuestPreview();
        });

        dailyTask.addSource(todayProgress, progressList -> {
            latestDailyProgress.clear();
            if (progressList != null) {
                latestDailyProgress.addAll(progressList);
            }
            renderDailyQuestPreview();
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

    private void renderDailyQuestPreview() {
        if (!latestProfileComplete) {
            dailyTask.setValue("Complete onboarding to unlock daily quests");
            return;
        }

        Map<String, QuestProgress> progressByQuest = new HashMap<>();
        for (QuestProgress progress : latestDailyProgress) {
            progressByQuest.put(progress.getQuestId(), progress);
        }

        QuestDefinition firstPendingQuest = null;
        QuestProgress firstPendingProgress = null;
        List<QuestDefinition> visibleDailyQuests = QuestRotation.visibleDailyQuests(
                latestQuestDefinitions,
                TrainingCalendar.startOfDay(System.currentTimeMillis())
        );
        for (QuestDefinition definition : visibleDailyQuests) {
            QuestProgress progress = progressByQuest.get(definition.getId());
            if (progress != null && progress.isCompleted() && !progress.isRewardClaimed()) {
                dailyTask.setValue("Claim reward: " + definition.getTitle()
                        + " (+" + definition.getRewardAmount() + " EXP)");
                return;
            }
            if (firstPendingQuest == null && (progress == null || !progress.isCompleted())) {
                firstPendingQuest = definition;
                firstPendingProgress = progress;
            }
        }

        if (firstPendingQuest == null) {
            dailyTask.setValue("Daily gates cleared. New quests arrive tomorrow.");
            return;
        }

        int progressCount = firstPendingProgress == null ? 0 : firstPendingProgress.getProgressCount();
        dailyTask.setValue(firstPendingQuest.getTitle() + " • "
                + Math.min(progressCount, firstPendingQuest.getTargetCount())
                + "/" + firstPendingQuest.getTargetCount());
    }
}
