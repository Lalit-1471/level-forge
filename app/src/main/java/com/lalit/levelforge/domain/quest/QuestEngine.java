package com.lalit.levelforge.domain.quest;

import com.lalit.levelforge.data.local.entity.ProgressionEvent;
import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.model.ProgressionEventType;
import com.lalit.levelforge.data.model.QuestMetricType;

public final class QuestEngine {

    private QuestEngine() {
    }

    public static boolean eventContributesToQuest(QuestDefinition definition, ProgressionEvent event) {
        if (definition == null || event == null || definition.getMetricType() == null) {
            return false;
        }
        QuestMetricType metricType = definition.getMetricType();
        ProgressionEventType eventType = event.getEventType();
        switch (metricType) {
            case WORKOUT_POSTED:
                return eventType == ProgressionEventType.WORKOUT_POSTED;
            case PROGRESSIVE_OVERLOAD:
                return eventType == ProgressionEventType.WEIGHT_PR
                        || eventType == ProgressionEventType.VOLUME_PR
                        || eventType == ProgressionEventType.REPS_PR;
            case WEIGHT_PR:
                return eventType == ProgressionEventType.WEIGHT_PR;
            case VOLUME_PR:
                return eventType == ProgressionEventType.VOLUME_PR;
            case REPS_PR:
                return eventType == ProgressionEventType.REPS_PR;
            default:
                return false;
        }
    }

    public static QuestProgress incrementProgress(QuestDefinition definition,
                                                  QuestProgress existingProgress,
                                                  long periodStartMillis,
                                                  long now) {
        QuestProgress progress = existingProgress == null
                ? new QuestProgress(definition.getId(), periodStartMillis, 0, false, false, now)
                : existingProgress;
        int nextCount = Math.min(definition.getTargetCount(), progress.getProgressCount() + 1);
        progress.setProgressCount(nextCount);
        progress.setCompleted(nextCount >= definition.getTargetCount());
        progress.setUpdatedAt(now);
        return progress;
    }
}
