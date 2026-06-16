package com.lalit.levelforge.data.repo;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.ExpEventDao;
import com.lalit.levelforge.data.local.dao.LevelStateDao;
import com.lalit.levelforge.data.local.dao.ProgressionEventDao;
import com.lalit.levelforge.data.local.dao.QuestDefinitionDao;
import com.lalit.levelforge.data.local.dao.QuestProgressDao;
import com.lalit.levelforge.data.local.entity.ExpEvent;
import com.lalit.levelforge.data.local.entity.LevelState;
import com.lalit.levelforge.data.local.entity.ProgressionEvent;
import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.model.ExpSourceType;
import com.lalit.levelforge.data.model.ProgressionEventType;
import com.lalit.levelforge.data.model.QuestMetricType;
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
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public QuestRepository(QuestDefinitionDao questDefinitionDao,
                           QuestProgressDao questProgressDao,
                           ProgressionEventDao progressionEventDao,
                           ExpEventDao expEventDao,
                           LevelStateDao levelStateDao) {
        this.questDefinitionDao = questDefinitionDao;
        this.questProgressDao = questProgressDao;
        this.progressionEventDao = progressionEventDao;
        this.expEventDao = expEventDao;
        this.levelStateDao = levelStateDao;
    }

    public LiveData<List<QuestDefinition>> observeActiveDefinitions() {
        return questDefinitionDao.observeActiveDefinitions();
    }

    public LiveData<List<QuestProgress>> observeProgressForPeriod(long periodStartMillis) {
        return questProgressDao.observeProgressForPeriod(periodStartMillis);
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
                QuestResetType.DAILY,
                QuestMetricType.WORKOUT_POSTED,
                1,
                QuestRewardType.EXP,
                45,
                true,
                10
        ));
        definitions.add(new QuestDefinition(
                "daily_six_sets",
                "Clear six sets",
                "Complete six working, warmup, or failure sets today.",
                QuestResetType.DAILY,
                QuestMetricType.SETS_COMPLETED,
                6,
                QuestRewardType.EXP,
                50,
                true,
                15
        ));
        definitions.add(new QuestDefinition(
                "daily_overload",
                "Trigger progressive overload",
                "Beat a previous best by weight, reps, or volume.",
                QuestResetType.DAILY,
                QuestMetricType.PROGRESSIVE_OVERLOAD,
                1,
                QuestRewardType.EXP,
                60,
                true,
                20
        ));
        definitions.add(new QuestDefinition(
                "weekly_three_workouts",
                "Train three times",
                "Post three workouts this week.",
                QuestResetType.WEEKLY,
                QuestMetricType.WORKOUT_POSTED,
                3,
                QuestRewardType.EXP,
                180,
                true,
                100
        ));
        definitions.add(new QuestDefinition(
                "weekly_eighteen_sets",
                "Build weekly volume",
                "Complete eighteen sets this week.",
                QuestResetType.WEEKLY,
                QuestMetricType.SETS_COMPLETED,
                18,
                QuestRewardType.EXP,
                160,
                true,
                105
        ));
        definitions.add(new QuestDefinition(
                "weekly_two_overloads",
                "Forge two new records",
                "Hit progressive overload twice this week.",
                QuestResetType.WEEKLY,
                QuestMetricType.PROGRESSIVE_OVERLOAD,
                2,
                QuestRewardType.EXP,
                220,
                true,
                110
        ));
        return definitions;
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
