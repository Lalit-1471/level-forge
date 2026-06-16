package com.lalit.levelforge.domain.quest;

import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.model.QuestResetType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class QuestRotation {

    private static final int DAILY_VISIBLE_COUNT = 4;
    private static final int WEEKLY_VISIBLE_COUNT = 3;
    private static final String DAILY_LOGIN_QUEST_ID = "daily_login";

    private QuestRotation() {
    }

    public static List<QuestDefinition> visibleDailyQuests(List<QuestDefinition> definitions,
                                                           long dayStartMillis) {
        List<QuestDefinition> dailyDefinitions = definitionsFor(definitions, QuestResetType.DAILY);
        List<QuestDefinition> visibleDefinitions = new ArrayList<>();
        QuestDefinition loginQuest = findById(dailyDefinitions, DAILY_LOGIN_QUEST_ID);
        if (loginQuest != null) {
            visibleDefinitions.add(loginQuest);
            dailyDefinitions.remove(loginQuest);
        }
        visibleDefinitions.addAll(rotatingSlice(
                dailyDefinitions,
                dayStartMillis,
                DAILY_VISIBLE_COUNT - visibleDefinitions.size(),
                1
        ));
        return visibleDefinitions;
    }

    public static List<QuestDefinition> visibleWeeklyQuests(List<QuestDefinition> definitions,
                                                            long weekStartMillis) {
        return rotatingSlice(
                definitionsFor(definitions, QuestResetType.WEEKLY),
                weekStartMillis,
                WEEKLY_VISIBLE_COUNT,
                7
        );
    }

    private static List<QuestDefinition> definitionsFor(List<QuestDefinition> definitions,
                                                        QuestResetType resetType) {
        List<QuestDefinition> filtered = new ArrayList<>();
        if (definitions == null) {
            return filtered;
        }
        for (QuestDefinition definition : definitions) {
            if (definition.getResetType() == resetType) {
                filtered.add(definition);
            }
        }
        filtered.sort(Comparator.comparingInt(QuestDefinition::getSortOrder));
        return filtered;
    }

    private static QuestDefinition findById(List<QuestDefinition> definitions, String questId) {
        for (QuestDefinition definition : definitions) {
            if (questId.equals(definition.getId())) {
                return definition;
            }
        }
        return null;
    }

    private static List<QuestDefinition> rotatingSlice(List<QuestDefinition> definitions,
                                                       long periodStartMillis,
                                                       int count,
                                                       int periodDays) {
        List<QuestDefinition> selected = new ArrayList<>();
        if (definitions.isEmpty() || count <= 0) {
            return selected;
        }

        int safeCount = Math.min(count, definitions.size());
        long periodIndex = TimeUnit.MILLISECONDS.toDays(periodStartMillis) / Math.max(1, periodDays);
        int offset = (int) Math.floorMod(periodIndex, definitions.size());
        for (int index = 0; index < safeCount; index++) {
            selected.add(definitions.get((offset + index) % definitions.size()));
        }
        selected.sort(Comparator.comparingInt(QuestDefinition::getSortOrder));
        return selected;
    }
}
