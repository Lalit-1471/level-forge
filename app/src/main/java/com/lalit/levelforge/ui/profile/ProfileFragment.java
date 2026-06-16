package com.lalit.levelforge.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lalit.levelforge.R;
import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.LevelState;
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.local.relation.WorkoutSetWithExercise;
import com.lalit.levelforge.data.model.RankTier;
import com.lalit.levelforge.databinding.FragmentProfileBinding;
import com.lalit.levelforge.databinding.ItemProfileBadgeBinding;
import com.lalit.levelforge.databinding.ItemWorkoutDetailExerciseBinding;
import com.lalit.levelforge.databinding.ItemWorkoutDetailPanelBinding;
import com.lalit.levelforge.databinding.ItemWorkoutDetailSetBinding;
import com.lalit.levelforge.databinding.ItemWorkoutSessionBinding;
import com.lalit.levelforge.domain.profile.HunterIdentityEvaluator;
import com.lalit.levelforge.domain.profile.ProfileBadge;
import com.lalit.levelforge.domain.progression.LevelCurve;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US);
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private List<WorkoutSession> latestSessions = new ArrayList<>();
    private List<WorkoutSetWithExercise> latestAllSetDetails = new ArrayList<>();
    private List<WorkoutSetWithExercise> selectedWorkoutDetails = new ArrayList<>();
    private LevelState latestLevelState;
    private long selectedSessionId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getCompletedSessions().observe(getViewLifecycleOwner(), this::renderWorkouts);
        viewModel.getLevelState().observe(getViewLifecycleOwner(), levelState -> {
            latestLevelState = levelState;
            renderPlayerStatus(levelState);
        });
        viewModel.getCompletedSetDetails().observe(getViewLifecycleOwner(), details -> {
            latestAllSetDetails = details == null ? new ArrayList<>() : new ArrayList<>(details);
            renderPlayerStatus(latestLevelState);
            renderBadges();
        });
        viewModel.getSelectedWorkoutSets().observe(getViewLifecycleOwner(), details -> {
            selectedWorkoutDetails = details == null ? new ArrayList<>() : new ArrayList<>(details);
            renderWorkouts(latestSessions);
        });
    }

    private void renderWorkouts(List<WorkoutSession> sessions) {
        latestSessions = sessions == null ? new ArrayList<>() : new ArrayList<>(sessions);
        binding.workoutListContainer.removeAllViews();
        int workoutCount = latestSessions.size();
        int totalExp = totalExp(latestSessions);
        binding.profileSummaryValue.setText(getString(R.string.profile_summary_value, workoutCount, totalExp));
        renderPlayerStatus(latestLevelState);
        renderBadges();

        if (workoutCount == 0) {
            binding.emptyWorkoutsText.setVisibility(View.VISIBLE);
            return;
        }

        binding.emptyWorkoutsText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (WorkoutSession session : latestSessions) {
            ItemWorkoutSessionBinding itemBinding = ItemWorkoutSessionBinding.inflate(inflater, binding.workoutListContainer, false);
            itemBinding.workoutTitle.setText(session.getTitle());
            itemBinding.workoutMeta.setText(getString(
                    R.string.profile_workout_meta,
                    dateFormat.format(new Date(session.getCompletedAt())),
                    formatDuration(session.getDurationSeconds())
            ));
            itemBinding.workoutExp.setText(getString(R.string.profile_workout_exp, session.getTotalExp()));
            itemBinding.getRoot().setOnClickListener(v -> {
                if (selectedSessionId == session.getId()) {
                    selectedSessionId = 0L;
                    selectedWorkoutDetails.clear();
                } else {
                    selectedSessionId = session.getId();
                }
                viewModel.selectSession(session.getId());
                renderWorkouts(latestSessions);
            });
            binding.workoutListContainer.addView(itemBinding.getRoot());
            if (selectedSessionId == session.getId()) {
                renderWorkoutDetail(inflater, session);
            }
        }
    }

    private void renderPlayerStatus(LevelState levelState) {
        if (binding == null) {
            return;
        }
        int totalExp = levelState == null ? 0 : levelState.getTotalExp();
        int level = levelState == null ? 1 : levelState.getLevel();
        RankTier rankTier = levelState == null ? RankTier.E : levelState.getRankTier();
        String fallbackTitle = levelState == null ? "Novice Hunter" : levelState.getActiveTitle();
        String activeTitle = HunterIdentityEvaluator.behaviorTitleFor(
                latestSessions,
                latestAllSetDetails,
                fallbackTitle
        );
        int expIntoLevel = LevelCurve.expIntoCurrentLevel(totalExp);
        int expForLevel = LevelCurve.expToAdvanceFromLevel(LevelCurve.levelForTotalExp(totalExp));
        int progress = expForLevel <= 0 ? 0 : (int) Math.round((expIntoLevel * 100.0) / expForLevel);

        binding.playerRankBadge.setText(rankLabel(rankTier));
        binding.playerLevelValue.setText(getString(R.string.profile_player_level, level));
        binding.playerTitleValue.setText(activeTitle);
        binding.playerExpBar.setProgress(Math.max(0, Math.min(100, progress)));
        binding.playerExpValue.setText(getString(R.string.profile_player_exp, expIntoLevel, expForLevel, totalExp));
    }

    private void renderBadges() {
        if (binding == null) {
            return;
        }
        binding.badgeListContainer.removeAllViews();
        List<ProfileBadge> badges = HunterIdentityEvaluator.collectedBadges(latestSessions, latestAllSetDetails);
        if (badges.isEmpty()) {
            binding.emptyBadgesText.setVisibility(View.VISIBLE);
            return;
        }

        binding.emptyBadgesText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (ProfileBadge badge : badges) {
            ItemProfileBadgeBinding badgeBinding = ItemProfileBadgeBinding.inflate(
                    inflater,
                    binding.badgeListContainer,
                    false
            );
            badgeBinding.badgeTitle.setText(badge.getTitle());
            badgeBinding.badgeDescription.setText(badge.getDescription());
            binding.badgeListContainer.addView(badgeBinding.getRoot());
        }
    }

    private void renderWorkoutDetail(LayoutInflater inflater, WorkoutSession session) {
        ItemWorkoutDetailPanelBinding detailBinding = ItemWorkoutDetailPanelBinding.inflate(inflater, binding.workoutListContainer, false);
        detailBinding.detailSummary.setText(getString(
                R.string.profile_workout_detail_summary,
                setCount(selectedWorkoutDetails),
                formatVolume(totalVolumeKg(selectedWorkoutDetails)),
                session.getTotalExp()
        ));

        if (selectedWorkoutDetails.isEmpty()) {
            detailBinding.detailSummary.setText(R.string.profile_workout_detail_loading);
        } else {
            long lastExerciseId = -1L;
            for (WorkoutSetWithExercise detail : selectedWorkoutDetails) {
                Exercise exercise = detail.getExercise();
                WorkoutSet workoutSet = detail.getWorkoutSet();
                if (exercise == null || workoutSet == null) {
                    continue;
                }
                if (exercise.getId() != lastExerciseId) {
                    ItemWorkoutDetailExerciseBinding exerciseBinding = ItemWorkoutDetailExerciseBinding.inflate(inflater, detailBinding.detailContainer, false);
                    exerciseBinding.detailExerciseName.setText(exercise.getName());
                    exerciseBinding.detailExerciseMeta.setText(pretty(exercise.getExerciseType().name()));
                    detailBinding.detailContainer.addView(exerciseBinding.getRoot());
                    lastExerciseId = exercise.getId();
                }

                ItemWorkoutDetailSetBinding setBinding = ItemWorkoutDetailSetBinding.inflate(inflater, detailBinding.detailContainer, false);
                setBinding.detailSetValue.setText(setLabel(workoutSet) + " • " + setDetails(workoutSet));
                setBinding.detailSetExp.setText(getString(R.string.profile_detail_set_exp, workoutSet.getTotalExp()));
                setBinding.detailSetBreakdown.setText(getString(
                        R.string.profile_detail_exp_breakdown,
                        workoutSet.getBaseExp(),
                        workoutSet.getEffortExp(),
                        workoutSet.getSetTypeExp(),
                        workoutSet.getOverloadExp(),
                        prLabels(workoutSet)
                ));
                detailBinding.detailContainer.addView(setBinding.getRoot());
            }
        }
        binding.workoutListContainer.addView(detailBinding.getRoot());
    }

    private int totalExp(List<WorkoutSession> sessions) {
        if (sessions == null) {
            return 0;
        }
        int total = 0;
        for (WorkoutSession session : sessions) {
            total += session.getTotalExp();
        }
        return total;
    }

    private int setCount(List<WorkoutSetWithExercise> details) {
        return details == null ? 0 : details.size();
    }

    private double totalVolumeKg(List<WorkoutSetWithExercise> details) {
        double volume = 0;
        if (details == null) {
            return volume;
        }
        for (WorkoutSetWithExercise detail : details) {
            WorkoutSet workoutSet = detail.getWorkoutSet();
            if (workoutSet != null) {
                volume += workoutSet.getWeightKg() * Math.max(1, workoutSet.getReps());
            }
        }
        return volume;
    }

    private String formatDuration(int durationSeconds) {
        if (durationSeconds <= 0) {
            return "0m";
        }
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        if (minutes == 0) {
            return seconds + "s";
        }
        return minutes + "m " + seconds + "s";
    }

    private String formatVolume(double value) {
        if (value <= 0) {
            return "0 kg";
        }
        return format(value) + " kg";
    }

    private String setLabel(WorkoutSet workoutSet) {
        if (workoutSet.getSetType() == null) {
            return String.valueOf(workoutSet.getSetNumber());
        }
        switch (workoutSet.getSetType()) {
            case WARMUP:
                return "W";
            case FAILURE:
                return "F";
            default:
                return String.valueOf(workoutSet.getSetNumber());
        }
    }

    private String setDetails(WorkoutSet workoutSet) {
        List<String> parts = new ArrayList<>();
        if (workoutSet.getWeightKg() > 0 && workoutSet.getReps() > 0) {
            parts.add(format(workoutSet.getWeightKg()) + " kg x " + workoutSet.getReps());
        } else {
            if (workoutSet.getWeightKg() > 0) {
                parts.add(format(workoutSet.getWeightKg()) + " kg");
            }
            if (workoutSet.getReps() > 0) {
                parts.add(workoutSet.getReps() + " reps");
            }
        }
        if (workoutSet.getDurationSeconds() > 0) {
            parts.add(workoutSet.getDurationSeconds() + " sec");
        }
        if (workoutSet.getDistanceMeters() > 0) {
            parts.add(format(workoutSet.getDistanceMeters()) + " m");
        }
        if (workoutSet.getAssistanceKg() > 0) {
            parts.add(format(workoutSet.getAssistanceKg()) + " kg assisted");
        }
        if (parts.isEmpty()) {
            parts.add("Completed");
        }
        if (workoutSet.getSetType() != null) {
            parts.add(pretty(workoutSet.getSetType().name()));
        }
        return String.join(" • ", parts);
    }

    private String prLabels(WorkoutSet workoutSet) {
        List<String> labels = new ArrayList<>();
        if (workoutSet.isWeightPr()) {
            labels.add("weight PR");
        }
        if (workoutSet.isVolumePr()) {
            labels.add("volume PR");
        }
        if (workoutSet.isRepsPr()) {
            labels.add("reps PR");
        }
        if (labels.isEmpty()) {
            return "no PR";
        }
        return String.join(", ", labels);
    }

    private String rankLabel(RankTier rankTier) {
        if (rankTier == RankTier.S_PLUS) {
            return "S+";
        }
        return rankTier == null ? "E" : rankTier.name();
    }

    private String pretty(String value) {
        String text = value.replace('_', ' ').replace(',', ' ').toLowerCase(Locale.US);
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

    private String format(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
