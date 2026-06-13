package com.lalit.levelforge.ui.workout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.lalit.levelforge.R;
import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.model.ExerciseType;
import com.lalit.levelforge.data.model.MuscleGroup;
import com.lalit.levelforge.data.model.SetType;
import com.lalit.levelforge.databinding.FragmentWorkoutLoggerBinding;
import com.lalit.levelforge.databinding.ItemLoggedSetBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WorkoutLoggerFragment extends Fragment {

    private FragmentWorkoutLoggerBinding binding;
    private WorkoutLoggerViewModel viewModel;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final List<Exercise> allExercises = new ArrayList<>();
    private final List<Exercise> visibleExercises = new ArrayList<>();
    private int lastTotalExp;
    private int lastSetCount;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimer();
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkoutLoggerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WorkoutLoggerViewModel.class);

        setupMuscleFilters();
        setupSetTypes();
        timerHandler.post(timerRunnable);

        viewModel.getExercises().observe(getViewLifecycleOwner(), this::renderExerciseChoices);
        viewModel.getLoggedSets().observe(getViewLifecycleOwner(), this::renderLoggedSets);
        viewModel.getTotalExp().observe(getViewLifecycleOwner(), exp -> {
            lastTotalExp = exp == null ? 0 : exp;
            binding.previewExpValue.setText(getString(R.string.workout_preview_exp, lastTotalExp));
            updateReviewSummary();
        });
        viewModel.getReviewMode().observe(getViewLifecycleOwner(), this::renderReviewMode);
        viewModel.getSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved != null && saved) {
                Toast.makeText(requireContext(), R.string.workout_saved, Toast.LENGTH_SHORT).show();
                viewModel.consumeSaved();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        binding.muscleFilterSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(this::filterExercises));
        binding.exerciseSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(this::renderSelectedExerciseFields));
        binding.addSetButton.setOnClickListener(v -> addSet());
        binding.finishWorkoutButton.setOnClickListener(v -> finishWorkout());
        binding.postWorkoutButton.setOnClickListener(v -> postWorkout());
        binding.discardWorkoutButton.setOnClickListener(v -> {
            viewModel.discardWorkout();
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void setupMuscleFilters() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, muscleFilterLabels());
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.muscleFilterSpinner.setAdapter(adapter);
    }

    private void setupSetTypes() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, setTypeLabels());
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.setTypeSpinner.setAdapter(adapter);
    }

    private void renderExerciseChoices(List<Exercise> loadedExercises) {
        allExercises.clear();
        if (loadedExercises != null) {
            allExercises.addAll(loadedExercises);
        }
        filterExercises();
    }

    private void filterExercises() {
        visibleExercises.clear();
        int selectedFilter = binding.muscleFilterSpinner.getSelectedItemPosition();
        MuscleGroup selectedMuscle = selectedFilter <= 0 ? null : MuscleGroup.values()[selectedFilter - 1];

        for (Exercise exercise : allExercises) {
            if (selectedMuscle == null || exercise.getPrimaryMuscleGroup() == selectedMuscle) {
                visibleExercises.add(exercise);
            }
        }

        List<String> labels = new ArrayList<>();
        for (Exercise exercise : visibleExercises) {
            labels.add(exercise.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.exerciseSpinner.setAdapter(adapter);
        renderSelectedExerciseFields();
    }

    private void renderSelectedExerciseFields() {
        Exercise exercise = selectedExercise();
        if (exercise == null) {
            return;
        }

        ExerciseType type = exercise.getExerciseType();
        boolean reps = type == ExerciseType.WEIGHT_REPS
                || type == ExerciseType.BODYWEIGHT_REPS
                || type == ExerciseType.WEIGHTED_BODYWEIGHT
                || type == ExerciseType.ASSISTED_BODYWEIGHT;
        boolean weight = type == ExerciseType.WEIGHT_REPS
                || type == ExerciseType.WEIGHTED_BODYWEIGHT
                || type == ExerciseType.WEIGHT_DURATION
                || type == ExerciseType.WEIGHT_DISTANCE;
        boolean duration = type == ExerciseType.DURATION
                || type == ExerciseType.WEIGHT_DURATION
                || type == ExerciseType.DISTANCE_DURATION;
        boolean distance = type == ExerciseType.DISTANCE_DURATION
                || type == ExerciseType.WEIGHT_DISTANCE;
        boolean assistance = type == ExerciseType.ASSISTED_BODYWEIGHT;

        binding.repsInput.setVisibility(reps ? View.VISIBLE : View.GONE);
        binding.weightInput.setVisibility(weight ? View.VISIBLE : View.GONE);
        binding.durationInput.setVisibility(duration ? View.VISIBLE : View.GONE);
        binding.distanceInput.setVisibility(distance ? View.VISIBLE : View.GONE);
        binding.assistanceInput.setVisibility(assistance ? View.VISIBLE : View.GONE);
        binding.exerciseTypeValue.setText(pretty(type.name()));
    }

    private void addSet() {
        Exercise exercise = selectedExercise();
        if (exercise == null) {
            Toast.makeText(requireContext(), R.string.workout_missing_exercise, Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.addSet(
                exercise,
                selectedSetType(),
                optionalInt(binding.repsInput),
                optionalDouble(binding.weightInput),
                optionalInt(binding.durationInput),
                optionalDouble(binding.distanceInput),
                optionalDouble(binding.assistanceInput),
                binding.progressiveOverloadCheckBox.isChecked()
        );
        clearSetInputs();
        renderSelectedExerciseFields();
    }

    private void finishWorkout() {
        if (lastSetCount == 0) {
            Toast.makeText(requireContext(), R.string.workout_no_sets, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.finishWorkout();
    }

    private void postWorkout() {
        String title = binding.workoutTitleInput.getText().toString().trim();
        if (title.isEmpty()) {
            title = "Strength workout";
        }
        viewModel.postWorkout(title, elapsedSeconds());
    }

    private void renderLoggedSets(List<LoggedSet> sets) {
        binding.loggedSetContainer.removeAllViews();
        lastSetCount = sets == null ? 0 : sets.size();
        updateReviewSummary();
        if (sets == null || sets.isEmpty()) {
            binding.emptySetsText.setVisibility(View.VISIBLE);
            return;
        }

        binding.emptySetsText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int index = 1;
        for (LoggedSet loggedSet : sets) {
            ItemLoggedSetBinding itemBinding = ItemLoggedSetBinding.inflate(inflater, binding.loggedSetContainer, false);
            itemBinding.loggedSetTitle.setText(index + ". " + loggedSet.getExercise().getName());
            itemBinding.loggedSetDetails.setText(setDetails(loggedSet));
            itemBinding.loggedSetExp.setText(loggedSet.getExp() + " EXP");
            binding.loggedSetContainer.addView(itemBinding.getRoot());
            index++;
        }
    }

    private void renderReviewMode(Boolean reviewMode) {
        boolean reviewing = reviewMode != null && reviewMode;
        binding.inputCard.setVisibility(reviewing ? View.GONE : View.VISIBLE);
        binding.finishWorkoutButton.setVisibility(reviewing ? View.GONE : View.VISIBLE);
        binding.reviewCard.setVisibility(reviewing ? View.VISIBLE : View.GONE);
        updateReviewSummary();
    }

    private void updateTimer() {
        String elapsed = formatElapsed(elapsedSeconds());
        binding.timerValue.setText(elapsed);
        updateReviewSummary();
    }

    private void updateReviewSummary() {
        if (binding == null) {
            return;
        }
        binding.reviewSummaryValue.setText(getString(
                R.string.workout_review_summary,
                lastSetCount,
                lastTotalExp,
                formatElapsed(elapsedSeconds())
        ));
    }

    private Exercise selectedExercise() {
        int position = binding.exerciseSpinner.getSelectedItemPosition();
        if (position < 0 || position >= visibleExercises.size()) {
            return null;
        }
        return visibleExercises.get(position);
    }

    private String setDetails(LoggedSet loggedSet) {
        List<String> parts = new ArrayList<>();
        if (loggedSet.getWorkoutSet().getReps() > 0) {
            parts.add(loggedSet.getWorkoutSet().getReps() + " reps");
        }
        if (loggedSet.getWorkoutSet().getWeightKg() > 0) {
            parts.add(format(loggedSet.getWorkoutSet().getWeightKg()) + " kg");
        }
        if (loggedSet.getWorkoutSet().getDurationSeconds() > 0) {
            parts.add(loggedSet.getWorkoutSet().getDurationSeconds() + " sec");
        }
        if (loggedSet.getWorkoutSet().getDistanceMeters() > 0) {
            parts.add(format(loggedSet.getWorkoutSet().getDistanceMeters()) + " m");
        }
        if (loggedSet.getWorkoutSet().getAssistanceKg() > 0) {
            parts.add(format(loggedSet.getWorkoutSet().getAssistanceKg()) + " kg assisted");
        }
        if (parts.isEmpty()) {
            parts.add("Completed");
        }
        parts.add(pretty(loggedSet.getWorkoutSet().getSetType().name()));
        return String.join(" • ", parts);
    }

    private SetType selectedSetType() {
        int position = binding.setTypeSpinner.getSelectedItemPosition();
        if (position == 1) {
            return SetType.WARMUP;
        }
        if (position == 2) {
            return SetType.FAILURE;
        }
        if (position == 3) {
            return SetType.AMRAP;
        }
        return SetType.NORMAL;
    }

    private List<String> muscleFilterLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("All muscles");
        for (MuscleGroup muscleGroup : MuscleGroup.values()) {
            labels.add(pretty(muscleGroup.name()));
        }
        return labels;
    }

    private List<String> setTypeLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("Working set");
        labels.add("Warmup");
        labels.add("Failure");
        labels.add("AMRAP");
        return labels;
    }

    private int optionalInt(EditText editText) {
        if (editText.getVisibility() == View.GONE) {
            return 0;
        }
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    private double optionalDouble(EditText editText) {
        if (editText.getVisibility() == View.GONE) {
            return 0;
        }
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? 0 : Double.parseDouble(value);
    }

    private void clearSetInputs() {
        binding.repsInput.setText("");
        binding.weightInput.setText("");
        binding.durationInput.setText("");
        binding.distanceInput.setText("");
        binding.assistanceInput.setText("");
        binding.progressiveOverloadCheckBox.setChecked(false);
    }

    private int elapsedSeconds() {
        return (int) ((System.currentTimeMillis() - viewModel.getStartedAtMillis()) / 1000L);
    }

    private String formatElapsed(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds);
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
        timerHandler.removeCallbacks(timerRunnable);
        binding = null;
    }
}
