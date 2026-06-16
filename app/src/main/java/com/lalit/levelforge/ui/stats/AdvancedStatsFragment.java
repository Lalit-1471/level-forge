package com.lalit.levelforge.ui.stats;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.lalit.levelforge.R;
import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.local.relation.WorkoutSetWithExercise;
import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.databinding.FragmentAdvancedStatsBinding;
import com.lalit.levelforge.databinding.ItemStatInsightBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
public class AdvancedStatsFragment extends Fragment {

    private enum Section {
        CALENDAR,
        MUSCLES,
        VOLUME,
        OVERLOAD,
        INSIGHTS
    }

    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEE, MMM d", Locale.US);
    private final SimpleDateFormat chartDateFormat = new SimpleDateFormat("M/d", Locale.US);
    private FragmentAdvancedStatsBinding binding;
    private StatsViewModel viewModel;
    private List<WorkoutSession> latestSessions = new ArrayList<>();
    private List<WorkoutSetWithExercise> latestSetDetails = new ArrayList<>();
    private Section selectedSection = Section.CALENDAR;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdvancedStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);
        setupActions();

        viewModel.getCompletedSessions().observe(getViewLifecycleOwner(), sessions -> {
            latestSessions = sessions == null ? new ArrayList<>() : new ArrayList<>(sessions);
            renderSelectedSection();
        });
        viewModel.getCompletedSetDetails().observe(getViewLifecycleOwner(), details -> {
            latestSetDetails = details == null ? new ArrayList<>() : new ArrayList<>(details);
            renderSelectedSection();
        });
    }

    private void setupActions() {
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        binding.calendarButton.setOnClickListener(v -> showSection(Section.CALENDAR));
        binding.musclesButton.setOnClickListener(v -> showSection(Section.MUSCLES));
        binding.volumeButton.setOnClickListener(v -> showSection(Section.VOLUME));
        binding.overloadButton.setOnClickListener(v -> showSection(Section.OVERLOAD));
        binding.insightsButton.setOnClickListener(v -> showSection(Section.INSIGHTS));
    }

    private void showSection(Section section) {
        selectedSection = section;
        renderSelectedSection();
    }

    private void renderSelectedSection() {
        if (binding == null) {
            return;
        }

        binding.advancedContentContainer.removeAllViews();
        binding.distributionCard.setVisibility(View.GONE);
        binding.lineChartCard.setVisibility(View.GONE);
        styleButtons();

        switch (selectedSection) {
            case MUSCLES:
                renderMuscleDistribution();
                break;
            case VOLUME:
                renderVolumeAnalytics();
                break;
            case OVERLOAD:
                renderOverloadDays();
                break;
            case INSIGHTS:
                renderDeepInsights();
                break;
            case CALENDAR:
            default:
                renderCalendar();
                break;
        }
    }

    private void renderCalendar() {
        binding.sectionTitle.setText(R.string.stats_calendar_title);
        binding.sectionDescription.setText(R.string.stats_calendar_description);
        List<WorkoutSession> sessions = new ArrayList<>(latestSessions);
        Collections.sort(sessions, (first, second) -> Long.compare(second.getCompletedAt(), first.getCompletedAt()));
        if (sessions.isEmpty()) {
            addCard(getString(R.string.stats_section_calendar), getString(R.string.stats_empty_value));
            return;
        }

        Map<String, List<WorkoutSession>> sessionsByDay = new LinkedHashMap<>();
        for (WorkoutSession session : sessions) {
            String day = dayFormat.format(new Date(session.getCompletedAt()));
            if (!sessionsByDay.containsKey(day)) {
                sessionsByDay.put(day, new ArrayList<>());
            }
            sessionsByDay.get(day).add(session);
        }

        for (Map.Entry<String, List<WorkoutSession>> entry : sessionsByDay.entrySet()) {
            List<String> workouts = new ArrayList<>();
            for (WorkoutSession session : entry.getValue()) {
                workouts.add(session.getTitle() + " • " + formatDuration(session.getDurationSeconds())
                        + " • " + session.getTotalExp() + " EXP");
            }
            addCard(entry.getKey(), String.join("\n", workouts));
        }
    }

    private void renderMuscleDistribution() {
        binding.sectionTitle.setText(R.string.stats_muscle_title);
        binding.sectionDescription.setText(R.string.stats_muscle_description);
        Map<String, Double> muscleSets = muscleSetDistribution();
        binding.distributionCard.setVisibility(View.VISIBLE);
        binding.distributionChart.setPoints(pointsFromMap(muscleSets, 7));

        String topMuscle = maxKey(muscleSets);
        addCard(getString(R.string.stats_top_muscle),
                topMuscle == null ? getString(R.string.stats_empty_value) : topMuscle + " • " + format(muscleSets.get(topMuscle)) + " sets");
        addCard("Muscle variety", muscleSets.isEmpty()
                ? getString(R.string.stats_empty_value)
                : muscleSets.size() + " muscle groups trained");
        addCard("Balance signal", balanceSignal(muscleSets));
    }

    private void renderVolumeAnalytics() {
        binding.sectionTitle.setText(R.string.stats_volume_title);
        binding.sectionDescription.setText(R.string.stats_volume_description);
        Map<Long, Double> volumeBySession = volumeBySession();
        List<WorkoutSession> sessions = chronologicalSessions();
        binding.lineChartCard.setVisibility(View.VISIBLE);
        binding.lineChart.setPoints(chartPointsForVolume(sessions, volumeBySession, 12));

        double total = totalVolume(volumeBySession);
        addCard("Total volume", formatVolume(total));
        addCard("Average volume", sessions.isEmpty() ? getString(R.string.stats_empty_value)
                : formatVolume(total / Math.max(1, sessions.size())) + " per workout");
        addCard("Highest volume workout", highestVolumeWorkout(volumeBySession));
        addCard("Volume trend", volumeTrend(sessions, volumeBySession));
    }

    private void renderOverloadDays() {
        binding.sectionTitle.setText(R.string.stats_overload_title);
        binding.sectionDescription.setText(R.string.stats_overload_description);
        List<String> weightEvents = overloadEvents(true);
        List<String> volumeEvents = overloadEvents(false);

        addCard("By highest weight", weightEvents.isEmpty()
                ? getString(R.string.stats_no_overload)
                : String.join("\n", lastValues(weightEvents, 8)));
        addCard("By highest volume", volumeEvents.isEmpty()
                ? getString(R.string.stats_no_overload)
                : String.join("\n", lastValues(volumeEvents, 8)));
        addCard("Overload focus", overloadFocus(weightEvents.size(), volumeEvents.size()));
    }

    private void renderDeepInsights() {
        binding.sectionTitle.setText(R.string.stats_deep_insights_title);
        binding.sectionDescription.setText(R.string.stats_deep_insights_description);
        addCard("Current streak", currentStreak() + " training days");
        addCard("Best training day", bestWeekday());
        addCard("Average duration", averageDuration());
        addCard("Average sets", averageSets());
        addCard("Highest EXP workout", bestSession());
        addCard("Suggested focus", suggestedFocus());
    }

    private void addCard(String label, String value) {
        ItemStatInsightBinding cardBinding = ItemStatInsightBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.advancedContentContainer,
                false
        );
        cardBinding.insightLabel.setText(label);
        cardBinding.insightValue.setText(value);
        binding.advancedContentContainer.addView(cardBinding.getRoot());
    }

    private void styleButtons() {
        styleButton(binding.calendarButton, selectedSection == Section.CALENDAR);
        styleButton(binding.musclesButton, selectedSection == Section.MUSCLES);
        styleButton(binding.volumeButton, selectedSection == Section.VOLUME);
        styleButton(binding.overloadButton, selectedSection == Section.OVERLOAD);
        styleButton(binding.insightsButton, selectedSection == Section.INSIGHTS);
    }

    private void styleButton(MaterialButton button, boolean selected) {
        int background = selected ? R.color.orange_400 : R.color.slate_900;
        int text = selected ? R.color.slate_950 : R.color.slate_200;
        int stroke = selected ? R.color.orange_400 : R.color.slate_700;
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), background)));
        button.setTextColor(ContextCompat.getColor(requireContext(), text));
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), stroke)));
    }

    private Map<String, Double> muscleSetDistribution() {
        Map<String, Double> distribution = new HashMap<>();
        for (WorkoutSetWithExercise detail : latestSetDetails) {
            Exercise exercise = detail.getExercise();
            if (exercise == null || exercise.getPrimaryMuscleGroup() == null) {
                continue;
            }
            String muscle = pretty(exercise.getPrimaryMuscleGroup().name());
            distribution.put(muscle, distribution.getOrDefault(muscle, 0.0) + 1.0);
        }
        return distribution;
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

    private List<WorkoutSession> chronologicalSessions() {
        List<WorkoutSession> sessions = new ArrayList<>(latestSessions);
        Collections.sort(sessions, Comparator.comparingLong(WorkoutSession::getCompletedAt));
        return sessions;
    }

    private List<StatsPoint> chartPointsForVolume(List<WorkoutSession> sessions,
                                                  Map<Long, Double> volumeBySession,
                                                  int limit) {
        List<StatsPoint> points = new ArrayList<>();
        for (WorkoutSession session : lastSessions(sessions, limit)) {
            double value = volumeBySession.containsKey(session.getId()) ? volumeBySession.get(session.getId()) : 0;
            points.add(new StatsPoint(chartDateFormat.format(new Date(session.getCompletedAt())), value));
        }
        return points;
    }

    private List<StatsPoint> pointsFromMap(Map<String, Double> values, int limit) {
        List<Map.Entry<String, Double>> entries = new ArrayList<>(values.entrySet());
        Collections.sort(entries, (first, second) -> Double.compare(second.getValue(), first.getValue()));
        List<StatsPoint> points = new ArrayList<>();
        int count = Math.min(limit, entries.size());
        for (int i = 0; i < count; i++) {
            Map.Entry<String, Double> entry = entries.get(i);
            points.add(new StatsPoint(entry.getKey(), entry.getValue()));
        }
        return points;
    }

    private List<WorkoutSession> lastSessions(List<WorkoutSession> sessions, int limit) {
        int start = Math.max(0, sessions.size() - limit);
        return sessions.subList(start, sessions.size());
    }

    private List<String> overloadEvents(boolean byWeight) {
        Map<Long, WorkoutSession> sessionsById = sessionsById();
        List<WorkoutSetWithExercise> details = chronologicalDetails();
        Map<Long, Double> bestByExercise = new HashMap<>();
        List<String> events = new ArrayList<>();

        for (WorkoutSetWithExercise detail : details) {
            WorkoutSet workoutSet = detail.getWorkoutSet();
            Exercise exercise = detail.getExercise();
            if (workoutSet == null || exercise == null) {
                continue;
            }
            double value = byWeight ? workoutSet.getWeightKg() : setVolume(workoutSet);
            if (value <= 0) {
                continue;
            }
            double previousBest = bestByExercise.containsKey(exercise.getId())
                    ? bestByExercise.get(exercise.getId())
                    : 0;
            if (previousBest > 0 && value > previousBest) {
                WorkoutSession session = sessionsById.get(workoutSet.getSessionId());
                String day = session == null ? "" : dayFormat.format(new Date(session.getCompletedAt())) + " • ";
                String suffix = byWeight ? format(value) + " kg" : formatVolume(value);
                events.add(day + exercise.getName() + " → " + suffix);
            }
            bestByExercise.put(exercise.getId(), Math.max(previousBest, value));
        }
        return events;
    }

    private List<WorkoutSetWithExercise> chronologicalDetails() {
        Map<Long, WorkoutSession> sessionsById = sessionsById();
        List<WorkoutSetWithExercise> details = new ArrayList<>(latestSetDetails);
        Collections.sort(details, (first, second) -> {
            WorkoutSession firstSession = first.getWorkoutSet() == null ? null : sessionsById.get(first.getWorkoutSet().getSessionId());
            WorkoutSession secondSession = second.getWorkoutSet() == null ? null : sessionsById.get(second.getWorkoutSet().getSessionId());
            long firstTime = firstSession == null ? 0 : firstSession.getCompletedAt();
            long secondTime = secondSession == null ? 0 : secondSession.getCompletedAt();
            if (firstTime != secondTime) {
                return Long.compare(firstTime, secondTime);
            }
            int exerciseCompare = Long.compare(
                    first.getWorkoutSet() == null ? 0 : first.getWorkoutSet().getExerciseId(),
                    second.getWorkoutSet() == null ? 0 : second.getWorkoutSet().getExerciseId()
            );
            if (exerciseCompare != 0) {
                return exerciseCompare;
            }
            return Integer.compare(
                    first.getWorkoutSet() == null ? 0 : first.getWorkoutSet().getSetNumber(),
                    second.getWorkoutSet() == null ? 0 : second.getWorkoutSet().getSetNumber()
            );
        });
        return details;
    }

    private Map<Long, WorkoutSession> sessionsById() {
        Map<Long, WorkoutSession> sessionsById = new HashMap<>();
        for (WorkoutSession session : latestSessions) {
            sessionsById.put(session.getId(), session);
        }
        return sessionsById;
    }

    private List<String> lastValues(List<String> values, int limit) {
        int start = Math.max(0, values.size() - limit);
        return values.subList(start, values.size());
    }

    private String highestVolumeWorkout(Map<Long, Double> volumeBySession) {
        WorkoutSession bestSession = null;
        double bestVolume = 0;
        for (WorkoutSession session : latestSessions) {
            double volume = volumeBySession.containsKey(session.getId()) ? volumeBySession.get(session.getId()) : 0;
            if (bestSession == null || volume > bestVolume) {
                bestSession = session;
                bestVolume = volume;
            }
        }
        return bestSession == null ? getString(R.string.stats_empty_value)
                : bestSession.getTitle() + " • " + formatVolume(bestVolume);
    }

    private String volumeTrend(List<WorkoutSession> sessions, Map<Long, Double> volumeBySession) {
        if (sessions.size() < 2) {
            return getString(R.string.stats_empty_value);
        }
        WorkoutSession previous = sessions.get(sessions.size() - 2);
        WorkoutSession latest = sessions.get(sessions.size() - 1);
        double previousVolume = volumeBySession.containsKey(previous.getId()) ? volumeBySession.get(previous.getId()) : 0;
        double latestVolume = volumeBySession.containsKey(latest.getId()) ? volumeBySession.get(latest.getId()) : 0;
        double delta = latestVolume - previousVolume;
        if (delta == 0) {
            return "Flat vs previous workout";
        }
        return (delta > 0 ? "+" : "") + formatVolume(delta) + " vs previous workout";
    }

    private String overloadFocus(int weightEvents, int volumeEvents) {
        if (weightEvents == 0 && volumeEvents == 0) {
            return getString(R.string.stats_no_overload);
        }
        if (volumeEvents >= weightEvents) {
            return "Volume is progressing fastest: " + volumeEvents + " volume PRs";
        }
        return "Load is progressing fastest: " + weightEvents + " weight PRs";
    }

    private int currentStreak() {
        if (latestSessions.isEmpty()) {
            return 0;
        }
        List<Long> trainingDays = uniqueTrainingDays();
        if (trainingDays.isEmpty()) {
            return 0;
        }
        Collections.sort(trainingDays, Collections.reverseOrder());
        int streak = 1;
        Calendar cursor = Calendar.getInstance();
        cursor.setTimeInMillis(trainingDays.get(0));
        for (int i = 1; i < trainingDays.size(); i++) {
            cursor.add(Calendar.DATE, -1);
            if (startOfDay(trainingDays.get(i)) == startOfDay(cursor.getTimeInMillis())) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    private List<Long> uniqueTrainingDays() {
        Map<Long, Boolean> days = new HashMap<>();
        for (WorkoutSession session : latestSessions) {
            days.put(startOfDay(session.getCompletedAt()), true);
        }
        return new ArrayList<>(days.keySet());
    }

    private long startOfDay(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private String bestWeekday() {
        Map<String, Double> weekdayCounts = new HashMap<>();
        SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE", Locale.US);
        for (WorkoutSession session : latestSessions) {
            String day = weekdayFormat.format(new Date(session.getCompletedAt()));
            weekdayCounts.put(day, weekdayCounts.getOrDefault(day, 0.0) + 1.0);
        }
        String weekday = maxKey(weekdayCounts);
        return weekday == null ? getString(R.string.stats_empty_value)
                : weekday + " • " + format(weekdayCounts.get(weekday)) + " workouts";
    }

    private String averageDuration() {
        if (latestSessions.isEmpty()) {
            return getString(R.string.stats_empty_value);
        }
        int totalSeconds = 0;
        for (WorkoutSession session : latestSessions) {
            totalSeconds += session.getDurationSeconds();
        }
        return formatDuration(totalSeconds / Math.max(1, latestSessions.size()));
    }

    private String averageSets() {
        if (latestSessions.isEmpty()) {
            return getString(R.string.stats_empty_value);
        }
        return format(latestSetDetails.size() / (double) Math.max(1, latestSessions.size())) + " sets per workout";
    }

    private String bestSession() {
        WorkoutSession bestSession = null;
        for (WorkoutSession session : latestSessions) {
            if (bestSession == null || session.getTotalExp() > bestSession.getTotalExp()) {
                bestSession = session;
            }
        }
        return bestSession == null ? getString(R.string.stats_empty_value)
                : bestSession.getTitle() + " • " + bestSession.getTotalExp() + " EXP";
    }

    private String suggestedFocus() {
        Map<String, Double> distribution = muscleSetDistribution();
        if (distribution.size() < 2) {
            return "Log a few more workouts to reveal balance suggestions";
        }
        String top = maxKey(distribution);
        double total = 0;
        for (double value : distribution.values()) {
            total += value;
        }
        if (top != null && distribution.get(top) / Math.max(1, total) > 0.5) {
            return "You are heavily favoring " + top + ". Add variety next session.";
        }
        return "Distribution looks balanced. Keep progressing priority lifts.";
    }

    private String balanceSignal(Map<String, Double> distribution) {
        if (distribution.isEmpty()) {
            return getString(R.string.stats_empty_value);
        }
        String top = maxKey(distribution);
        double total = 0;
        for (double value : distribution.values()) {
            total += value;
        }
        double share = top == null ? 0 : distribution.get(top) / Math.max(1, total);
        return top + " leads with " + Math.round(share * 100) + "% of logged sets";
    }

    private String maxKey(Map<String, Double> values) {
        String bestKey = null;
        double bestValue = 0;
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            if (bestKey == null || entry.getValue() > bestValue) {
                bestKey = entry.getKey();
                bestValue = entry.getValue();
            }
        }
        return bestKey;
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
        if (value == 0) {
            return "0 kg";
        }
        String prefix = value > 0 ? "" : "-";
        return prefix + format(Math.abs(value)) + " kg";
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
