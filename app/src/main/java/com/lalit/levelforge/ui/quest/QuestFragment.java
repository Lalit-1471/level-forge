package com.lalit.levelforge.ui.quest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.lalit.levelforge.R;
import com.lalit.levelforge.data.local.entity.QuestDefinition;
import com.lalit.levelforge.data.local.entity.QuestProgress;
import com.lalit.levelforge.data.model.QuestResetType;
import com.lalit.levelforge.data.model.QuestRewardType;
import com.lalit.levelforge.databinding.FragmentQuestsBinding;
import com.lalit.levelforge.databinding.ItemQuestCardBinding;

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
    private List<QuestProgress> dailyProgress = new ArrayList<>();
    private List<QuestProgress> weeklyProgress = new ArrayList<>();

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
        viewModel.getDailyProgress().observe(getViewLifecycleOwner(), value -> {
            dailyProgress = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
        viewModel.getWeeklyProgress().observe(getViewLifecycleOwner(), value -> {
            weeklyProgress = value == null ? new ArrayList<>() : new ArrayList<>(value);
            renderQuests();
        });
    }

    private void renderQuests() {
        if (binding == null) {
            return;
        }

        binding.dailyQuestContainer.removeAllViews();
        binding.weeklyQuestContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        List<QuestDefinition> dailyDefinitions = definitionsFor(QuestResetType.DAILY);
        List<QuestDefinition> weeklyDefinitions = definitionsFor(QuestResetType.WEEKLY);
        Map<String, QuestProgress> dailyProgressByQuest = progressByQuestId(dailyProgress);
        Map<String, QuestProgress> weeklyProgressByQuest = progressByQuestId(weeklyProgress);

        renderSummary(dailyDefinitions, dailyProgressByQuest, weeklyDefinitions, weeklyProgressByQuest);
        renderQuestSection(inflater, binding.dailyQuestContainer, dailyDefinitions,
                dailyProgressByQuest, viewModel.getTodayStartMillis());
        renderQuestSection(inflater, binding.weeklyQuestContainer, weeklyDefinitions,
                weeklyProgressByQuest, viewModel.getWeekStartMillis());

        binding.emptyQuestText.setVisibility(definitions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void renderSummary(List<QuestDefinition> dailyDefinitions,
                               Map<String, QuestProgress> dailyProgressByQuest,
                               List<QuestDefinition> weeklyDefinitions,
                               Map<String, QuestProgress> weeklyProgressByQuest) {
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

    private void bindQuestCard(ItemQuestCardBinding itemBinding, QuestDefinition definition,
                               QuestProgress progress, long periodStartMillis) {
        int progressCount = progress == null ? 0 : progress.getProgressCount();
        int targetCount = Math.max(1, definition.getTargetCount());
        boolean completed = progress != null && progress.isCompleted();
        boolean claimed = progress != null && progress.isRewardClaimed();
        int progressPercent = (int) Math.round((Math.min(progressCount, targetCount) * 100.0) / targetCount);

        itemBinding.questTitle.setText(definition.getTitle());
        itemBinding.questDescription.setText(definition.getDescription());
        itemBinding.questMeta.setText(getString(
                R.string.quests_card_meta,
                pretty(definition.getMetricType().name()),
                rewardText(definition)
        ));
        itemBinding.questProgressText.setText(getString(
                R.string.quests_card_progress,
                Math.min(progressCount, targetCount),
                targetCount
        ));
        itemBinding.questProgressBar.setProgress(progressPercent);
        itemBinding.questStatePill.setText(stateText(completed, claimed));

        MaterialCardView card = itemBinding.getRoot();
        int strokeColor = ContextCompat.getColor(requireContext(), R.color.slate_700);
        if (completed && claimed) {
            strokeColor = ContextCompat.getColor(requireContext(), R.color.emerald_400);
        } else if (completed) {
            strokeColor = ContextCompat.getColor(requireContext(), R.color.orange_400);
        }
        card.setStrokeColor(strokeColor);

        if (completed && !claimed) {
            itemBinding.questClaimButton.setEnabled(true);
            itemBinding.questClaimButton.setText(getString(
                    R.string.quests_claim_reward,
                    definition.getRewardAmount()
            ));
            itemBinding.questClaimButton.setOnClickListener(v ->
                    viewModel.claimReward(definition.getId(), periodStartMillis));
        } else {
            itemBinding.questClaimButton.setOnClickListener(null);
            itemBinding.questClaimButton.setEnabled(false);
            itemBinding.questClaimButton.setText(claimButtonText(completed, claimed));
        }
    }

    private List<QuestDefinition> definitionsFor(QuestResetType resetType) {
        List<QuestDefinition> filtered = new ArrayList<>();
        for (QuestDefinition definition : definitions) {
            if (definition.getResetType() == resetType) {
                filtered.add(definition);
            }
        }
        return filtered;
    }

    private Map<String, QuestProgress> progressByQuestId(List<QuestProgress> progressList) {
        Map<String, QuestProgress> progressByQuest = new HashMap<>();
        for (QuestProgress progress : progressList) {
            progressByQuest.put(progress.getQuestId(), progress);
        }
        return progressByQuest;
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

    private String claimButtonText(boolean completed, boolean claimed) {
        if (claimed) {
            return getString(R.string.quests_claimed);
        }
        if (completed) {
            return getString(R.string.quests_claim_available);
        }
        return getString(R.string.quests_in_progress);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
