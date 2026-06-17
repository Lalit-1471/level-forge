package com.lalit.levelforge.ui.quest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestObjective;
import com.lalit.levelforge.data.local.entity.QuestObjectiveProgress;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.local.entity.StreakState;
import com.lalit.levelforge.data.repo.QuestRepository;
import com.lalit.levelforge.domain.calendar.TrainingCalendar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuestViewModel extends ViewModel {

    private final QuestRepository questRepository;
    private final long todayStartMillis;
    private final long weekStartMillis;
    private final long biweekStartMillis;

    @Inject
    public QuestViewModel(QuestRepository questRepository) {
        this.questRepository = questRepository;
        long now = System.currentTimeMillis();
        todayStartMillis = TrainingCalendar.startOfDay(now);
        weekStartMillis = TrainingCalendar.startOfWeek(now);
        biweekStartMillis = TrainingCalendar.startOfBiweek(now);
        questRepository.seedDefaultQuestDefinitions();
        questRepository.recordDailyLogin();
    }

    public LiveData<List<QuestDefinition>> getQuestDefinitions() {
        return questRepository.observeActiveDefinitions();
    }

    public LiveData<List<QuestProgress>> getDailyProgress() {
        return questRepository.observeProgressForPeriod(todayStartMillis);
    }

    public LiveData<List<QuestProgress>> getWeeklyProgress() {
        return questRepository.observeProgressForPeriod(weekStartMillis);
    }

    public LiveData<List<QuestProgress>> getBiweeklyProgress() {
        return questRepository.observeProgressForPeriod(biweekStartMillis);
    }

    public LiveData<List<QuestObjective>> getQuestObjectives() {
        return questRepository.observeQuestObjectives();
    }

    public LiveData<List<QuestObjectiveProgress>> getWeeklyObjectiveProgress() {
        return questRepository.observeObjectiveProgressForPeriod(weekStartMillis);
    }

    public LiveData<List<QuestObjectiveProgress>> getBiweeklyObjectiveProgress() {
        return questRepository.observeObjectiveProgressForPeriod(biweekStartMillis);
    }

    public LiveData<StreakState> getStreakState() {
        return questRepository.observeStreakState();
    }

    public long getTodayStartMillis() {
        return todayStartMillis;
    }

    public long getWeekStartMillis() {
        return weekStartMillis;
    }

    public long getBiweekStartMillis() {
        return biweekStartMillis;
    }

    public void claimReward(String questId, long periodStartMillis) {
        questRepository.claimReward(questId, periodStartMillis);
    }
}
