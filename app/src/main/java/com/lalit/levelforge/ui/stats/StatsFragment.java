package com.lalit.levelforge.ui.stats;

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
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.local.relation.WorkoutSetWithExercise;
import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.data.model.MuscleGroup;
import com.lalit.levelforge.databinding.FragmentStatsBinding;
import com.lalit.levelforge.databinding.ItemStatInsightBinding;
import com.lalit.levelforge.databinding.ItemStatMetricBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatsFragment extends Fragment {

    private final SimpleDateFormat chartDateFormat = new SimpleDateFormat("M/d", Locale.US);
    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;
    private List<WorkoutSession> latestSessions = new ArrayList<>();
    private List<WorkoutSetWithExercise> latestSetDetails = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        viewModel.getCompletedSessions().observe(getViewLifecycleOwner(), sessions -> {
            latestSessions = sessions == null ? new ArrayList<>() : new ArrayList<>(sessions);
            renderStats();
        });
        viewModel.getCompletedSetDetails().observe(getViewLifecycleOwner(), details -> {
            latestSetDetails = details == null ? new ArrayList<>() : new ArrayList<>(details);
            renderStats();
        });
    }

    private void renderStats() {
        if (binding == null) {
            return;
        }

        List<WorkoutSession> chronologicalSessions = chronologicalSessions();
        Map<Long, Double> volumeBySession = volumeBySession();

        int workouts = latestSessions.size();
        int sets = latestSetDetails.size();
        double totalVolume = totalVolume(volumeBySession);
        int totalExp = totalExp(latestSessions);

        bindMetric(binding.workoutsMetric, getString(R.string.stats_metric_workouts), String.valueOf(workouts));
        bindMetric(binding.setsMetric, getString(R.string.stats_metric_sets), String.valueOf(sets));
        bindMetric(binding.volumeMetric, getString(R.string.stats_metric_volume), formatVolume(totalVolume));
        bindMetric(binding.expMetric, getString(R.string.stats_metric_exp), totalExp + " EXP");

        binding.volumeChart.setPoints(chartPointsForVolume(chronologicalSessions, volumeBySession));
        binding.expChart.setPoints(chartPointsForExp(chronologicalSessions));

        renderInsights();
    }

    private void bindMetric(ItemStatMetricBinding metricBinding, String label, String value) {
        metricBinding.metricLabel.setText(label);
        metricBinding.metricValue.setText(value);
    }

    private void renderInsights() {
        binding.insightsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        addInsight(inflater, getString(R.string.stats_favorite_exercise), favoriteExercise());
        addInsight(inflater, getString(R.string.stats_top_muscle), topMuscle());
        addInsight(inflater, getString(R.string.stats_best_effort), bestEffort());
        addInsight(inflater, getString(R.string.stats_best_session), bestSession());
    }

    private void addInsight(LayoutInflater inflater, String label, String value) {
        ItemStatInsightBinding insightBinding = ItemStatInsightBinding.inflate(inflater, binding.insightsContainer, false);
        insightBinding.insightLabel.setText(label);
        insightBinding.insightValue.setText(value);
        binding.insightsContainer.addView(insightBinding.getRoot());
    }

    private List<WorkoutSession> chronologicalSessions() {
        List<WorkoutSession> sessions = new ArrayList<>(latestSessions);
        Collections.sort(sessions, Comparator.comparingLong(WorkoutSession::getCompletedAt));
        return sessions;
    }

    private Map<Long, Double> volumeBySession() {
        Map<Long, Double> volumeBySession = new LinkedHashMap<>();
        for (WorkoutSetWithExercise detail : latestSetDetails) {
            WorkoutSet workoutSet = detail.getWorkoutSet();
            if (workoutSet == null) {
                continue;
            }
            double currentVolume = volumeBySession.containsKey(workoutSet.getSessionId())
                    ? volumeBySession.get(workoutSet.getSessionId())
                    : 0;
            volumeBySession.put(workoutSet.getSessionId(), currentVolume + setVolume(workoutSet));
        }
        return volumeBySession;
    }

    private List<StatsPoint> chartPointsForVolume(List<WorkoutSession> sessions, Map<Long, Double> volumeBySession) {
        List<StatsPoint> points = new ArrayList<>();
        for (WorkoutSession session : lastSessions(sessions, 8)) {
            double value = volumeBySession.containsKey(session.getId()) ? volumeBySession.get(session.getId()) : 0;
            points.add(new StatsPoint(chartDateFormat.format(new Date(session.getCompletedAt())), value));
        }
        return points;
    }

    private List<StatsPoint> chartPointsForExp(List<WorkoutSession> sessions) {
        List<StatsPoint> points = new ArrayList<>();
        for (WorkoutSession session : lastSessions(sessions, 8)) {
            points.add(new StatsPoint(
                    chartDateFormat.format(new Date(session.getCompletedAt())),
                    session.getTotalExp()
            ));
        }
        return points;
    }

    private List<WorkoutSession> lastSessions(List<WorkoutSession> sessions, int limit) {
        int start = Math.max(0, sessions.size() - limit);
        return sessions.subList(start, sessions.size());
    }

    private String favoriteExercise() {
        Map<String, Integer> setCounts = new HashMap<>();
        for (WorkoutSetWithExercise detail : latestSetDetails) {
            Exercise exercise = detail.getExercise();
            if (exercise == null) {
                continue;
            }
            setCounts.put(exercise.getName(), setCounts.getOrDefault(exercise.getName(), 0) + 1);
        }
        String name = maxKey(setCounts);
        return name == null ? getString(R.string.stats_empty_value) : name + " • " + setCounts.get(name) + " sets";
    }

    private String topMuscle() {
        Map<String, Integer> setCounts = new HashMap<>();
        for (WorkoutSetWithExercise detail : latestSetDetails) {
            Exercise exercise = detail.getExercise();
            if (exercise == null || exercise.getPrimaryMuscleGroup() == null) {
                continue;
            }
            MuscleGroup muscleGroup = exercise.getPrimaryMuscleGroup();
            String key = pretty(muscleGroup.name());
            setCounts.put(key, setCounts.getOrDefault(key, 0) + 1);
        }
        String muscle = maxKey(setCounts);
        return muscle == null ? getString(R.string.stats_empty_value) : muscle + " • " + setCounts.get(muscle) + " sets";
    }

    private String bestEffort() {
        WorkoutSetWithExercise bestDetail = null;
        double bestScore = 0;
        for (WorkoutSetWithExercise detail : latestSetDetails) {
            WorkoutSet workoutSet = detail.getWorkoutSet();
            Exercise exercise = detail.getExercise();
            if (workoutSet == null || exercise == null) {
                continue;
            }
            double score = effortScore(exercise.getExerciseType(), workoutSet);
            if (score > bestScore) {
                bestScore = score;
                bestDetail = detail;
            }
        }
        if (bestDetail == null) {
            return getString(R.string.stats_empty_value);
        }
        return bestDetail.getExercise().getName() + " • " + setDetails(bestDetail.getWorkoutSet());
    }

    private String bestSession() {
        WorkoutSession bestSession = null;
        for (WorkoutSession session : latestSessions) {
            if (bestSession == null || session.getTotalExp() > bestSession.getTotalExp()) {
                bestSession = session;
            }
        }
        if (bestSession == null) {
            return getString(R.string.stats_empty_value);
        }
        return bestSession.getTitle() + " • " + bestSession.getTotalExp() + " EXP";
    }

    private String maxKey(Map<String, Integer> values) {
        String bestKey = null;
        int bestValue = 0;
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            if (bestKey == null || entry.getValue() > bestValue) {
                bestKey = entry.getKey();
                bestValue = entry.getValue();
            }
        }
        return bestKey;
    }

    private double effortScore(ExerciseType exerciseType, WorkoutSet workoutSet) {
        if (exerciseType == null) {
            return workoutSet.getReps()
                    + workoutSet.getWeightKg()
                    + workoutSet.getDurationSeconds()
                    + workoutSet.getDistanceMeters();
        }

        switch (exerciseType) {
            case WEIGHT_REPS:
            case WEIGHTED_BODYWEIGHT:
                return workoutSet.getWeightKg() * workoutSet.getReps();
            case ASSISTED_BODYWEIGHT:
                return workoutSet.getReps() * 100.0 - workoutSet.getAssistanceKg();
            case BODYWEIGHT_REPS:
                return workoutSet.getReps();
            case DURATION:
                return workoutSet.getDurationSeconds();
            case WEIGHT_DURATION:
                return workoutSet.getWeightKg() * workoutSet.getDurationSeconds();
            case DISTANCE_DURATION:
                return workoutSet.getDistanceMeters() + workoutSet.getDurationSeconds() / 10.0;
            case WEIGHT_DISTANCE:
                return workoutSet.getWeightKg() * workoutSet.getDistanceMeters();
            default:
                return 0;
        }
    }

    private int totalExp(List<WorkoutSession> sessions) {
        int total = 0;
        for (WorkoutSession session : sessions) {
            total += session.getTotalExp();
        }
        return total;
    }

    private double totalVolume(Map<Long, Double> volumeBySession) {
        double total = 0;
        for (double volume : volumeBySession.values()) {
            total += volume;
        }
        return total;
    }

    private double setVolume(WorkoutSet workoutSet) {
        return workoutSet.getWeightKg() * Math.max(1, workoutSet.getReps());
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
        return parts.isEmpty() ? getString(R.string.stats_empty_value) : String.join(" • ", parts);
    }

    private String formatVolume(double value) {
        if (value <= 0) {
            return "0 kg";
        }
        return format(value) + " kg";
    }

    private String format(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.US, "%.1f", value);
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
