package com.lalit.levelforge.data.repo;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.QuestDefinitionDao;
import com.lalit.levelforge.data.local.dao.QuestProgressDao;
import com.lalit.levelforge.data.local.entity.ProgressionEvent;
import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.model.QuestMetricType;
import com.lalit.levelforge.data.model.QuestResetType;
import com.lalit.levelforge.data.model.QuestRewardType;
import com.lalit.levelforge.domain.calendar.TrainingCalendar;
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
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public QuestRepository(QuestDefinitionDao questDefinitionDao,
                           QuestProgressDao questProgressDao) {
        this.questDefinitionDao = questDefinitionDao;
        this.questProgressDao = questProgressDao;
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
        diskExecutor.execute(() -> {
            long now = System.currentTimeMillis();
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
        });
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
}
