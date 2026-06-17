package com.lalit.levelforge.ui.quest;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.lalit.levelforge.R;
import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestObjective;
import com.lalit.levelforge.data.local.entity.QuestObjectiveProgress;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.local.entity.StreakState;
import com.lalit.levelforge.data.model.QuestRarity;
import com.lalit.levelforge.data.model.QuestResetType;
import com.lalit.levelforge.data.model.QuestRewardType;
import com.lalit.levelforge.databinding.FragmentQuestsBinding;
import com.lalit.levelforge.databinding.ItemQuestCardBinding;
import com.lalit.levelforge.domain.quest.QuestRotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuestFragment extends Fragment {

    private FragmentQuestsBinding binding;
    private QuestViewModel viewModel;
    private List<QuestDefinition> definitions = new ArrayList<>();
    private List<QuestObjective> objectives = new ArrayList<>();
    private List<QuestProgress> dailyProgress = new ArrayList<>();
    private List<QuestProgress> weeklyProgress = new ArrayList<>();
    private List<QuestProgress> biweeklyProgress = new ArrayList<>();
    private List<QuestObjectiveProgress> weeklyObjectiveProgress = new ArrayList<>();
    private List<QuestObjectiveProgress> biweeklyObjectiveProgress = new ArrayList<>();
    private StreakState streakState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentQuestsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(QuestViewModel.class);
        viewModel.getQuestDefinitions().observe(getViewLifecycleOwner(), value -> {
            definitions = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
        viewModel.getQuestObjectives().observe(getViewLifecycleOwner(), value -> {
            objectives = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
        viewModel.getDailyProgress().observe(getViewLifecycleOwner(), value -> {
            dailyProgress = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
        viewModel.getWeeklyProgress().observe(getViewLifecycleOwner(), value -> {
            weeklyProgress = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
        viewModel.getBiweeklyProgress().observe(getViewLifecycleOwner(), value -> {
            biweeklyProgress = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
        viewModel.getWeeklyObjectiveProgress().observe(getViewLifecycleOwner(), value -> {
            weeklyObjectiveProgress = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
        viewModel.getBiweeklyObjectiveProgress().observe(getViewLifecycleOwner(), value -> {
            biweeklyObjectiveProgress = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
        viewModel.getStreakState().observe(getViewLifecycleOwner(), value -> {
            streakState = value;
            renderQuests();
        });
    }

    private void renderQuests() {
        if (binding == null) {
            return;
        }

        binding.dailyQuestContainer.removeAllViews();
        binding.weeklyQuestContainer.removeAllViews();
        binding.bossQuestContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        List<QuestDefinition> dailyDefinitions = QuestRotation.visibleDailyQuests(
                definitions,
                viewModel.getTodayStartMillis()
        );
        List<QuestDefinition> weeklyDefinitions = QuestRotation.visibleWeeklyQuests(
                definitions,
                viewModel.getWeekStartMillis()
        );
        List<QuestDefinition> bossDefinitions = QuestRotation.visibleBossQuests(
                definitions,
                viewModel.getWeekStartMillis(),
                viewModel.getBiweekStartMillis()
        );
        Map<String, QuestProgress> dailyProgressByQuest = progressByQuestId(dailyProgress);
        Map<String, QuestProgress> weeklyProgressByQuest = progressByQuestId(weeklyProgress);
        Map<String, QuestProgress> biweeklyProgressByQuest = progressByQuestId(biweeklyProgress);
        Map<String, QuestObjectiveProgress> weeklyProgressByObjective = progressByObjectiveId(weeklyObjectiveProgress);
        Map<String, QuestObjectiveProgress> biweeklyProgressByObjective = progressByObjectiveId(biweeklyObjectiveProgress);

        renderStreakState();
        renderSummary(dailyDefinitions, dailyProgressByQuest, weeklyDefinitions, weeklyProgressByQuest,
                bossDefinitions, weeklyProgressByQuest, biweeklyProgressByQuest);
        renderQuestSection(inflater, binding.dailyQuestContainer, dailyDefinitions,
                dailyProgressByQuest, viewModel.getTodayStartMillis());
        renderQuestSection(inflater, binding.weeklyQuestContainer, weeklyDefinitions,
                weeklyProgressByQuest, viewModel.getWeekStartMillis());
        renderBossSection(inflater, bossDefinitions, weeklyProgressByQuest, biweeklyProgressByQuest,
                weeklyProgressByObjective, biweeklyProgressByObjective);

        boolean hasBossQuests = !bossDefinitions.isEmpty();
        binding.bossQuestTitle.setVisibility(hasBossQuests ? View.VISIBLE : View.GONE);
        binding.bossQuestContainer.setVisibility(hasBossQuests ? View.VISIBLE : View.GONE);

        binding.emptyQuestText.setVisibility(definitions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void renderStreakState() {
        int currentStreak = streakState == null ? 0 : streakState.getCurrentStreakDays();
        int longestStreak = streakState == null ? 0 : streakState.getLongestStreakDays();
        int shields = streakState == null ? 0 : streakState.getStreakShields();
        binding.streakSummaryValue.setText(getString(R.string.quests_streak_days, currentStreak));
        binding.bestStreakValue.setText(getString(R.string.quests_best_streak, longestStreak));
        binding.shieldSummaryValue.setText(getString(R.string.quests_shields_available, shields));
        binding.shieldHintText.setText(shields > 0
                ? getString(R.string.quests_shield_ready)
                : getString(R.string.quests_shield_earn));
    }

    private void renderSummary(List<QuestDefinition> dailyDefinitions,
                               Map<String, QuestProgress> dailyProgressByQuest,
                               List<QuestDefinition> weeklyDefinitions,
                               Map<String, QuestProgress> weeklyProgressByQuest,
                               List<QuestDefinition> bossDefinitions,
                               Map<String, QuestProgress> weeklyBossProgressByQuest,
                               Map<String, QuestProgress> biweeklyBossProgressByQuest) {
        int dailyCompleted = completedCount(dailyDefinitions, dailyProgressByQuest);
        int weeklyCompleted = completedCount(weeklyDefinitions, weeklyProgressByQuest);
        binding.dailySummaryValue.setText(getString(
                R.string.quests_summary_progress,
                dailyCompleted,
                dailyDefinitions.size()
        ));
        binding.weeklySummaryValue.setText(getString(
                R.string.quests_summary_progress,
                weeklyCompleted,
                weeklyDefinitions.size()
        ));
        binding.rewardSummaryValue.setText(getString(
                R.string.quests_rewards_waiting,
                rewardsWaiting(dailyDefinitions, dailyProgressByQuest)
                        + rewardsWaiting(weeklyDefinitions, weeklyProgressByQuest)
                        + bossRewardsWaiting(bossDefinitions, weeklyBossProgressByQuest, biweeklyBossProgressByQuest)
        ));
    }

    private void renderQuestSection(LayoutInflater inflater, ViewGroup container,
                                    List<QuestDefinition> sectionDefinitions,
                                    Map<String, QuestProgress> progressByQuest,
                                    long periodStartMillis) {
        if (sectionDefinitions.isEmpty()) {
            return;
        }
        for (QuestDefinition definition : sectionDefinitions) {
            QuestProgress progress = progressByQuest.get(definition.getId());
            ItemQuestCardBinding itemBinding = ItemQuestCardBinding.inflate(inflater, container, false);
            bindQuestCard(itemBinding, definition, progress, periodStartMillis);
            container.addView(itemBinding.getRoot());
        }
    }

    private void renderBossSection(LayoutInflater inflater,
                                   List<QuestDefinition> bossDefinitions,
                                   Map<String, QuestProgress> weeklyProgressByQuest,
                                   Map<String, QuestProgress> biweeklyProgressByQuest,
                                   Map<String, QuestObjectiveProgress> weeklyProgressByObjective,
                                   Map<String, QuestObjectiveProgress> biweeklyProgressByObjective) {
        for (QuestDefinition definition : bossDefinitions) {
            long periodStartMillis = QuestRotation.periodStartFor(
                    definition,
                    viewModel.getWeekStartMillis(),
                    viewModel.getBiweekStartMillis()
            );
            boolean biweekly = periodStartMillis == viewModel.getBiweekStartMillis();
            Map<String, QuestProgress> questProgressMap = biweekly ? biweeklyProgressByQuest : weeklyProgressByQuest;
            Map<String, QuestObjectiveProgress> objectiveProgressMap =
                    biweekly ? biweeklyProgressByObjective : weeklyProgressByObjective;
            ItemQuestCardBinding itemBinding = ItemQuestCardBinding.inflate(
                    inflater,
                    binding.bossQuestContainer,
                    false
            );
            bindQuestCard(itemBinding, definition, questProgressMap.get(definition.getId()),
                    periodStartMillis, objectivesForQuest(definition.getId()), objectiveProgressMap);
            binding.bossQuestContainer.addView(itemBinding.getRoot());
        }
    }

    private void bindQuestCard(ItemQuestCardBinding itemBinding, QuestDefinition definition,
                               QuestProgress progress, long periodStartMillis) {
        bindQuestCard(itemBinding, definition, progress, periodStartMillis, null, null);
    }

    private void bindQuestCard(ItemQuestCardBinding itemBinding, QuestDefinition definition,
                               QuestProgress progress, long periodStartMillis,
                               List<QuestObjective> questObjectives,
                               Map<String, QuestObjectiveProgress> progressByObjective) {
        int progressCount = progress == null ? 0 : progress.getProgressCount();
        int targetCount = questObjectives == null || questObjectives.isEmpty()
                ? Math.max(1, definition.getTargetCount())
                : questObjectives.size();
        boolean completed = progress != null && progress.isCompleted();
        boolean claimed = progress != null && progress.isRewardClaimed();
        int progressPercent = (int) Math.round((Math.min(progressCount, targetCount) * 100.0) / targetCount);

        itemBinding.questTitle.setText(definition.getTitle());
        itemBinding.questDescription.setText(definition.getDescription());
        QuestRarity rarity = definition.getRarity() == null ? QuestRarity.COMMON : definition.getRarity();
        int rarityColor = rarityColor(rarity);
        itemBinding.questRarityPill.setText(pretty(rarity.name()));
        itemBinding.questRarityPill.setTextColor(rarityColor);
        itemBinding.questMeta.setText(getString(
                R.string.quests_card_meta,
                definition.getRarity() == QuestRarity.BOSS ? "Boss Trial" : pretty(definition.getMetricType().name()),
                rewardText(definition)
        ));
        itemBinding.questMeta.setTextColor(rarityColor);
        itemBinding.questProgressText.setText(getString(
                R.string.quests_card_progress,
                Math.min(progressCount, targetCount),
                targetCount
        ));
        itemBinding.questProgressBar.setProgress(progressPercent);
        itemBinding.questProgressBar.setProgressTintList(ColorStateList.valueOf(rarityColor));
        itemBinding.questStatePill.setText(stateText(completed, claimed));

        MaterialCardView card = itemBinding.getRoot();
        card.setStrokeColor(rarityColor);
        renderObjectives(itemBinding, questObjectives, progressByObjective, rarityColor);

        if (completed && !claimed) {
            itemBinding.questClaimButton.setVisibility(View.VISIBLE);
            itemBinding.questClaimButton.setEnabled(true);
            itemBinding.questClaimButton.setText(getString(
                    R.string.quests_claim_reward,
                    definition.getRewardAmount()
            ));
            itemBinding.questClaimButton.setOnClickListener(v ->
                    viewModel.claimReward(definition.getId(), periodStartMillis));
        } else {
            itemBinding.questClaimButton.setOnClickListener(null);
            itemBinding.questClaimButton.setVisibility(View.GONE);
        }
    }

    private void renderObjectives(ItemQuestCardBinding itemBinding,
                                  List<QuestObjective> questObjectives,
                                  Map<String, QuestObjectiveProgress> progressByObjective,
                                  int rarityColor) {
        itemBinding.objectiveContainer.removeAllViews();
        if (questObjectives == null || questObjectives.isEmpty()) {
            itemBinding.objectiveContainer.setVisibility(View.GONE);
            return;
        }
        itemBinding.objectiveContainer.setVisibility(View.VISIBLE);
        for (QuestObjective objective : questObjectives) {
            QuestObjectiveProgress progress = progressByObjective == null
                    ? null
                    : progressByObjective.get(objective.getId());
            int progressCount = progress == null ? 0 : progress.getProgressCount();
            int targetCount = Math.max(1, objective.getTargetCount());
            TextView objectiveText = new TextView(requireContext());
            objectiveText.setText(getString(
                    R.string.quests_objective_progress,
                    objective.getLabel(),
                    Math.min(progressCount, targetCount),
                    targetCount
            ));
            objectiveText.setTextColor(progress != null && progress.isCompleted()
                    ? rarityColor
                    : ContextCompat.getColor(requireContext(), R.color.slate_300));
            objectiveText.setTextSize(13);
            objectiveText.setPadding(0, 4, 0, 4);
            itemBinding.objectiveContainer.addView(objectiveText);
        }
    }

    private Map<String, QuestProgress> progressByQuestId(List<QuestProgress> progressList) {
        Map<String, QuestProgress> progressByQuest = new HashMap<>();
        for (QuestProgress progress : progressList) {
            progressByQuest.put(progress.getQuestId(), progress);
        }
        return progressByQuest;
    }

    private Map<String, QuestObjectiveProgress> progressByObjectiveId(
            List<QuestObjectiveProgress> progressList) {
        Map<String, QuestObjectiveProgress> progressByObjective = new HashMap<>();
        for (QuestObjectiveProgress progress : progressList) {
            progressByObjective.put(progress.getObjectiveId(), progress);
        }
        return progressByObjective;
    }

    private List<QuestObjective> objectivesForQuest(String questId) {
        List<QuestObjective> filtered = new ArrayList<>();
        for (QuestObjective objective : objectives) {
            if (questId.equals(objective.getQuestId())) {
                filtered.add(objective);
            }
        }
        return filtered;
    }

    private int completedCount(List<QuestDefinition> sectionDefinitions,
                               Map<String, QuestProgress> progressByQuest) {
        int count = 0;
        for (QuestDefinition definition : sectionDefinitions) {
            QuestProgress progress = progressByQuest.get(definition.getId());
            if (progress != null && progress.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    private int rewardsWaiting(List<QuestDefinition> sectionDefinitions,
                               Map<String, QuestProgress> progressByQuest) {
        int count = 0;
        for (QuestDefinition definition : sectionDefinitions) {
            QuestProgress progress = progressByQuest.get(definition.getId());
            if (progress != null && progress.isCompleted() && !progress.isRewardClaimed()) {
                count++;
            }
        }
        return count;
    }

    private int bossRewardsWaiting(List<QuestDefinition> bossDefinitions,
                                   Map<String, QuestProgress> weeklyProgressByQuest,
                                   Map<String, QuestProgress> biweeklyProgressByQuest) {
        int count = 0;
        for (QuestDefinition definition : bossDefinitions) {
            Map<String, QuestProgress> progressByQuest = definition.getResetType() == QuestResetType.BIWEEKLY
                    ? biweeklyProgressByQuest
                    : weeklyProgressByQuest;
            QuestProgress progress = progressByQuest.get(definition.getId());
            if (progress != null && progress.isCompleted() && !progress.isRewardClaimed()) {
                count++;
            }
        }
        return count;
    }

    private String rewardText(QuestDefinition definition) {
        if (definition.getRewardType() == QuestRewardType.EXP) {
            return getString(R.string.quests_reward_exp, definition.getRewardAmount());
        }
        return getString(R.string.quests_reward_title);
    }

    private String stateText(boolean completed, boolean claimed) {
        if (claimed) {
            return getString(R.string.quests_state_claimed);
        }
        if (completed) {
            return getString(R.string.quests_state_ready);
        }
        return getString(R.string.quests_state_active);
    }

    private String pretty(String value) {
        String text = value.replace('_', ' ').toLowerCase(Locale.US);
        String[] words = text.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    private int rarityColor(QuestRarity rarity) {
        switch (rarity) {
            case RARE:
                return ContextCompat.getColor(requireContext(), R.color.quest_rare);
            case EPIC:
                return ContextCompat.getColor(requireContext(), R.color.quest_epic);
            case BOSS:
                return ContextCompat.getColor(requireContext(), R.color.quest_boss);
            case COMMON:
            default:
                return ContextCompat.getColor(requireContext(), R.color.quest_common);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
