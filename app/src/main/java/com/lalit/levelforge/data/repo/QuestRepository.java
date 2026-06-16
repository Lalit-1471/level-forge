package com.lalit.levelforge.data.repo;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.ExpEventDao;
import com.lalit.levelforge.data.local.dao.LevelStateDao;
import com.lalit.levelforge.data.local.dao.ProgressionEventDao;
import com.lalit.levelforge.data.local.dao.QuestDefinitionDao;
import com.lalit.levelforge.data.local.dao.QuestProgressDao;
import com.lalit.levelforge.data.local.dao.StreakStateDao;
import com.lalit.levelforge.data.local.entity.ExpEvent;
import com.lalit.levelforge.data.local.entity.LevelState;
import com.lalit.levelforge.data.local.entity.ProgressionEvent;
import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.local.entity.StreakState;
import com.lalit.levelforge.data.model.ExpSourceType;
import com.lalit.levelforge.data.model.ProgressionEventType;
import com.lalit.levelforge.data.model.QuestMetricType;
import com.lalit.levelforge.data.model.QuestRarity;
import com.lalit.levelforge.data.model.QuestResetType;
import com.lalit.levelforge.data.model.QuestRewardType;
import com.lalit.levelforge.domain.calendar.TrainingCalendar;
import com.lalit.levelforge.domain.progression.LevelCurve;
import com.lalit.levelforge.domain.progression.RankEvaluator;
import com.lalit.levelforge.domain.progression.TitleCatalog;
import com.lalit.levelforge.domain.quest.QuestEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QuestRepository {

    private final QuestDefinitionDao questDefinitionDao;
    private final QuestProgressDao questProgressDao;
    private final ProgressionEventDao progressionEventDao;
    private final ExpEventDao expEventDao;
    private final LevelStateDao levelStateDao;
    private final StreakStateDao streakStateDao;
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public QuestRepository(QuestDefinitionDao questDefinitionDao,
                           QuestProgressDao questProgressDao,
                           ProgressionEventDao progressionEventDao,
                           ExpEventDao expEventDao,
                           LevelStateDao levelStateDao,
                           StreakStateDao streakStateDao) {
        this.questDefinitionDao = questDefinitionDao;
        this.questProgressDao = questProgressDao;
        this.progressionEventDao = progressionEventDao;
        this.expEventDao = expEventDao;
        this.levelStateDao = levelStateDao;
        this.streakStateDao = streakStateDao;
    }

    public LiveData<List<QuestDefinition>> observeActiveDefinitions() {
        return questDefinitionDao.observeActiveDefinitions();
    }

    public LiveData<List<QuestProgress>> observeProgressForPeriod(long periodStartMillis) {
        return questProgressDao.observeProgressForPeriod(periodStartMillis);
    }

    public LiveData<StreakState> observeStreakState() {
        return streakStateDao.observeStreakState();
    }

    public void seedDefaultQuestDefinitions() {
        diskExecutor.execute(() -> questDefinitionDao.upsertAll(defaultDefinitions()));
    }

    public void applyProgressionEvent(ProgressionEvent event) {
        diskExecutor.execute(() -> applyProgressionEventOnDisk(event, System.currentTimeMillis()));
    }

    public void recordDailyLogin() {
        diskExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            long dayStart = TrainingCalendar.startOfDay(now);
            long dayEnd = TrainingCalendar.endOfDay(now);
            int loginsToday = progressionEventDao.countEventsBetween(ProgressionEventType.LOGIN, dayStart, dayEnd);
            if (loginsToday > 0) {
                return;
            }
            updateLoginStreak(dayStart, now);
            ProgressionEvent event = new ProgressionEvent(
                    ProgressionEventType.LOGIN,
                    0,
                    0,
                    0,
                    1,
                    "Daily login",
                    now
            );
            progressionEventDao.insert(event);
            applyProgressionEventOnDisk(event, now);
        });
    }

    public void claimReward(String questId, long periodStartMillis) {
        diskExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            QuestDefinition definition = questDefinitionDao.getDefinition(questId);
            QuestProgress progress = questProgressDao.getProgress(questId, periodStartMillis);
            if (definition == null || progress == null
                    || !progress.isCompleted()
                    || progress.isRewardClaimed()
                    || definition.getRewardType() != QuestRewardType.EXP) {
                return;
            }

            ExpSourceType sourceType = definition.getResetType() == QuestResetType.WEEKLY
                    ? ExpSourceType.WEEKLY_TASK
                    : ExpSourceType.DAILY_TASK;
            expEventDao.insert(new ExpEvent(
                    sourceType,
                    progress.getId(),
                    definition.getRewardAmount(),
                    definition.getTitle(),
                    now
            ));
            progress.setRewardClaimed(true);
            progress.setUpdatedAt(now);
            questProgressDao.upsert(progress);
            updateLevelState(expEventDao.getTotalExp(), now);
        });
    }

    private void applyProgressionEventOnDisk(ProgressionEvent event, long now) {
        List<QuestDefinition> definitions = questDefinitionDao.getActiveDefinitions();
        for (QuestDefinition definition : definitions) {
            if (!QuestEngine.eventContributesToQuest(definition, event)) {
                continue;
            }
            long periodStartMillis = periodStartFor(definition.getResetType(), event.getCreatedAt());
            QuestProgress existingProgress = questProgressDao.getProgress(definition.getId(), periodStartMillis);
            QuestProgress nextProgress = QuestEngine.incrementProgress(
                    definition,
                    existingProgress,
                    periodStartMillis,
                    now
            );
            questProgressDao.upsert(nextProgress);
        }
    }

    private long periodStartFor(QuestResetType resetType, long eventTimeMillis) {
        if (resetType == QuestResetType.WEEKLY) {
            return TrainingCalendar.startOfWeek(eventTimeMillis);
        }
        if (resetType == QuestResetType.ONCE) {
            return 0;
        }
        return TrainingCalendar.startOfDay(eventTimeMillis);
    }

    private List<QuestDefinition> defaultDefinitions() {
        List<QuestDefinition> definitions = new ArrayList<>();
        definitions.add(new QuestDefinition(
                "daily_login",
                "Enter the gate",
                "Open Level Forge once today.",
                QuestRarity.COMMON,
                QuestResetType.DAILY,
                QuestMetricType.LOGIN,
                1,
                QuestRewardType.EXP,
                20,
                true,
                5
        ));
        definitions.add(new QuestDefinition(
                "daily_workout_posted",
                "Complete one workout",
                "Post any workout today.",
                QuestRarity.COMMON,
                QuestResetType.DAILY,
                QuestMetricType.WORKOUT_POSTED,
                1,
                QuestRewardType.EXP,
                45,
                true,
                10
        ));
        definitions.add(new QuestDefinition(
                "daily_short_hunt",
                "Short hunt",
                "Complete four sets today, even if it is a quick session.",
                QuestRarity.COMMON,
                QuestResetType.DAILY,
                QuestMetricType.SETS_COMPLETED,
                4,
                QuestRewardType.EXP,
                35,
                true,
                12
        ));
        definitions.add(new QuestDefinition(
                "daily_six_sets",
                "Clear six sets",
                "Complete six working, warmup, or failure sets today.",
                QuestRarity.COMMON,
                QuestResetType.DAILY,
                QuestMetricType.SETS_COMPLETED,
                6,
                QuestRewardType.EXP,
                50,
                true,
                15
        ));
        definitions.add(new QuestDefinition(
                "daily_ten_sets",
                "Volume gate",
                "Complete ten sets today.",
                QuestRarity.RARE,
                QuestResetType.DAILY,
                QuestMetricType.SETS_COMPLETED,
                10,
                QuestRewardType.EXP,
                95,
                true,
                18
        ));
        definitions.add(new QuestDefinition(
                "daily_overload",
                "Trigger progressive overload",
                "Beat a previous best by weight, reps, or volume.",
                QuestRarity.RARE,
                QuestResetType.DAILY,
                QuestMetricType.PROGRESSIVE_OVERLOAD,
                1,
                QuestRewardType.EXP,
                85,
                true,
                20
        ));
        definitions.add(new QuestDefinition(
                "daily_double_overload",
                "Double awakening",
                "Trigger progressive overload twice today.",
                QuestRarity.EPIC,
                QuestResetType.DAILY,
                QuestMetricType.PROGRESSIVE_OVERLOAD,
                2,
                QuestRewardType.EXP,
                140,
                true,
                25
        ));
        definitions.add(new QuestDefinition(
                "daily_weight_pr",
                "Raise the ceiling",
                "Set a new highest-weight PR today.",
                QuestRarity.RARE,
                QuestResetType.DAILY,
                QuestMetricType.WEIGHT_PR,
                1,
                QuestRewardType.EXP,
                95,
                true,
                30
        ));
        definitions.add(new QuestDefinition(
                "daily_volume_pr",
                "Forge more volume",
                "Set a new highest-volume PR today.",
                QuestRarity.RARE,
                QuestResetType.DAILY,
                QuestMetricType.VOLUME_PR,
                1,
                QuestRewardType.EXP,
                95,
                true,
                35
        ));
        definitions.add(new QuestDefinition(
                "daily_reps_pr",
                "Break the rep limit",
                "Set a new reps PR today.",
                QuestRarity.RARE,
                QuestResetType.DAILY,
                QuestMetricType.REPS_PR,
                1,
                QuestRewardType.EXP,
                90,
                true,
                40
        ));
        definitions.add(new QuestDefinition(
                "weekly_three_workouts",
                "Train three times",
                "Post three workouts this week.",
                QuestRarity.COMMON,
                QuestResetType.WEEKLY,
                QuestMetricType.WORKOUT_POSTED,
                3,
                QuestRewardType.EXP,
                180,
                true,
                100
        ));
        definitions.add(new QuestDefinition(
                "weekly_four_workouts",
                "Four-gate week",
                "Post four workouts this week.",
                QuestRarity.RARE,
                QuestResetType.WEEKLY,
                QuestMetricType.WORKOUT_POSTED,
                4,
                QuestRewardType.EXP,
                260,
                true,
                102
        ));
        definitions.add(new QuestDefinition(
                "weekly_eighteen_sets",
                "Build weekly volume",
                "Complete eighteen sets this week.",
                QuestRarity.COMMON,
                QuestResetType.WEEKLY,
                QuestMetricType.SETS_COMPLETED,
                18,
                QuestRewardType.EXP,
                160,
                true,
                105
        ));
        definitions.add(new QuestDefinition(
                "weekly_no_pr_required",
                "Discipline over ego",
                "Complete eighteen sets this week. No PR is required for this reward.",
                QuestRarity.COMMON,
                QuestResetType.WEEKLY,
                QuestMetricType.SETS_COMPLETED,
                18,
                QuestRewardType.EXP,
                190,
                true,
                106
        ));
        definitions.add(new QuestDefinition(
                "weekly_recovery_block",
                "Recovery contract",
                "Log eight light, cardio, core, or mobility-friendly sets this week.",
                QuestRarity.RARE,
                QuestResetType.WEEKLY,
                QuestMetricType.SETS_COMPLETED,
                8,
                QuestRewardType.EXP,
                210,
                true,
                107
        ));
        definitions.add(new QuestDefinition(
                "weekly_thirty_sets",
                "High-volume raid",
                "Complete thirty sets this week.",
                QuestRarity.EPIC,
                QuestResetType.WEEKLY,
                QuestMetricType.SETS_COMPLETED,
                30,
                QuestRewardType.EXP,
                360,
                true,
                108
        ));
        definitions.add(new QuestDefinition(
                "weekly_two_overloads",
                "Forge two new records",
                "Hit progressive overload twice this week.",
                QuestRarity.RARE,
                QuestResetType.WEEKLY,
                QuestMetricType.PROGRESSIVE_OVERLOAD,
                2,
                QuestRewardType.EXP,
                220,
                true,
                110
        ));
        definitions.add(new QuestDefinition(
                "weekly_five_overloads",
                "Overload raid",
                "Trigger progressive overload five times this week.",
                QuestRarity.BOSS,
                QuestResetType.WEEKLY,
                QuestMetricType.PROGRESSIVE_OVERLOAD,
                5,
                QuestRewardType.EXP,
                520,
                true,
                115
        ));
        definitions.add(new QuestDefinition(
                "weekly_two_weight_prs",
                "Iron ceiling raid",
                "Set two highest-weight PRs this week.",
                QuestRarity.EPIC,
                QuestResetType.WEEKLY,
                QuestMetricType.WEIGHT_PR,
                2,
                QuestRewardType.EXP,
                360,
                true,
                120
        ));
        definitions.add(new QuestDefinition(
                "weekly_two_volume_prs",
                "Capacity raid",
                "Set two highest-volume PRs this week.",
                QuestRarity.EPIC,
                QuestResetType.WEEKLY,
                QuestMetricType.VOLUME_PR,
                2,
                QuestRewardType.EXP,
                360,
                true,
                125
        ));
        definitions.add(new QuestDefinition(
                "weekly_two_reps_prs",
                "Endurance raid",
                "Set two reps PRs this week.",
                QuestRarity.EPIC,
                QuestResetType.WEEKLY,
                QuestMetricType.REPS_PR,
                2,
                QuestRewardType.EXP,
                330,
                true,
                130
        ));
        return definitions;
    }

    private void updateLoginStreak(long todayStart, long now) {
        StreakState state = streakStateDao.getStreakState();
        if (state == null) {
            state = new StreakState();
        }

        long lastLoginDay = state.getLastLoginDayStartMillis();
        int previousStreak = state.getCurrentStreakDays();
        int nextStreak = previousStreak <= 0 ? 1 : previousStreak;
        int shields = state.getStreakShields();

        if (lastLoginDay <= 0) {
            nextStreak = 1;
        } else {
            int daysSinceLastLogin = TrainingCalendar.daysBetween(lastLoginDay, todayStart);
            if (daysSinceLastLogin == 1) {
                nextStreak = previousStreak + 1;
            } else if (daysSinceLastLogin > 1) {
                int missedDays = daysSinceLastLogin - 1;
                if (shields >= missedDays) {
                    shields -= missedDays;
                    nextStreak = previousStreak + 1;
                } else {
                    shields = 0;
                    nextStreak = 1;
                }
            }
        }

        if (nextStreak > previousStreak && nextStreak % 7 == 0) {
            shields = Math.min(2, shields + 1);
        }

        state.setCurrentStreakDays(nextStreak);
        state.setLongestStreakDays(Math.max(state.getLongestStreakDays(), nextStreak));
        state.setStreakShields(shields);
        state.setLastLoginDayStartMillis(todayStart);
        state.setUpdatedAt(now);
        streakStateDao.upsert(state);
    }

    private void updateLevelState(int totalExp, long now) {
        int level = LevelCurve.levelForTotalExp(totalExp);
        LevelState state = new LevelState(
                level,
                totalExp,
                RankEvaluator.rankForLevel(level),
                TitleCatalog.titleFor(level, RankEvaluator.rankForLevel(level)),
                now
        );
        levelStateDao.upsert(state);
    }
}
