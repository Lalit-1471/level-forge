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
import com.lalit.levelforge.databinding.ItemExerciseBinding;
import com.lalit.levelforge.databinding.ItemLoggedSetBinding;
import com.lalit.levelforge.databinding.ItemSetEditorBinding;
import com.lalit.levelforge.databinding.ItemWorkoutExerciseBinding;

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
    private List<LoggedExercise> latestLoggedExercises = new ArrayList<>();
    private long editingExerciseId = -1L;
    private int lastTotalExp;

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
        timerHandler.post(timerRunnable);

        viewModel.getExercises().observe(getViewLifecycleOwner(), this::renderExercisePicker);
        viewModel.getLoggedExercises().observe(getViewLifecycleOwner(), this::renderWorkout);
        viewModel.getTotalExp().observe(getViewLifecycleOwner(), exp -> {
            lastTotalExp = exp == null ? 0 : exp;
            binding.previewExpValue.setText(getString(R.string.workout_preview_exp, lastTotalExp));
            updateReviewSummary();
        });
        viewModel.getReviewMode().observe(getViewLifecycleOwner(), reviewing -> {
            if (reviewing != null && reviewing) {
                showReview();
            }
        });
        viewModel.getSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved != null && saved) {
                Toast.makeText(requireContext(), R.string.workout_saved, Toast.LENGTH_SHORT).show();
                viewModel.consumeSaved();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        binding.addExerciseButton.setOnClickListener(v -> showPicker());
        binding.cancelPickerButton.setOnClickListener(v -> showWorkout());
        binding.muscleFilterSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(this::filterExercises));
        binding.finishWorkoutButton.setOnClickListener(v -> finishWorkout());
        binding.postWorkoutButton.setOnClickListener(v -> viewModel.postWorkout("Workout", elapsedSeconds()));
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

    private void renderExercisePicker(List<Exercise> loadedExercises) {
        allExercises.clear();
        if (loadedExercises != null) {
            allExercises.addAll(loadedExercises);
        }
        filterExercises();
    }

    private void filterExercises() {
        if (binding == null) {
            return;
        }
        visibleExercises.clear();
        int selectedFilter = binding.muscleFilterSpinner.getSelectedItemPosition();
        MuscleGroup selectedMuscle = selectedFilter <= 0 ? null : MuscleGroup.values()[selectedFilter - 1];

        for (Exercise exercise : allExercises) {
            if (selectedMuscle == null || exercise.getPrimaryMuscleGroup() == selectedMuscle) {
                visibleExercises.add(exercise);
            }
        }

        binding.exercisePickerContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Exercise exercise : visibleExercises) {
            ItemExerciseBinding itemBinding = ItemExerciseBinding.inflate(inflater, binding.exercisePickerContainer, false);
            itemBinding.exerciseName.setText(exercise.getName());
            itemBinding.exerciseType.setText(pretty(exercise.getExerciseType().name()));
            itemBinding.exerciseMuscles.setText(primaryAndSecondary(exercise));
            itemBinding.exerciseExp.setText(exercise.getBaseExp() + " base EXP");
            itemBinding.getRoot().setOnClickListener(v -> {
                viewModel.addExercise(exercise);
                editingExerciseId = exercise.getId();
                showWorkout();
            });
            binding.exercisePickerContainer.addView(itemBinding.getRoot());
        }
    }

    private void renderWorkout(List<LoggedExercise> loggedExercises) {
        latestLoggedExercises = loggedExercises == null ? new ArrayList<>() : new ArrayList<>(loggedExercises);
        binding.emptyWorkoutText.setVisibility(latestLoggedExercises.isEmpty() ? View.VISIBLE : View.GONE);
        binding.exerciseContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (LoggedExercise loggedExercise : latestLoggedExercises) {
            ItemWorkoutExerciseBinding itemBinding = ItemWorkoutExerciseBinding.inflate(inflater, binding.exerciseContainer, false);
            itemBinding.exerciseName.setText(loggedExercise.getExercise().getName());
            itemBinding.exerciseType.setText(pretty(loggedExercise.getExercise().getExerciseType().name()));
            renderSets(itemBinding, loggedExercise);
            if (editingExerciseId == loggedExercise.getExercise().getId()) {
                renderEditor(itemBinding, loggedExercise.getExercise());
            }
            itemBinding.addSetButton.setOnClickListener(v -> {
                editingExerciseId = loggedExercise.getExercise().getId();
                renderWorkout(latestLoggedExercises);
            });
            binding.exerciseContainer.addView(itemBinding.getRoot());
        }
        renderReviewExercises();
        updateReviewSummary();
    }

    private void renderSets(ItemWorkoutExerciseBinding itemBinding, LoggedExercise loggedExercise) {
        itemBinding.setContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int index = 0;
        for (LoggedSet loggedSet : loggedExercise.getSets()) {
            ItemLoggedSetBinding setBinding = ItemLoggedSetBinding.inflate(inflater, itemBinding.setContainer, false);
            setBinding.loggedSetTitle.setText("Set " + loggedSet.getWorkoutSet().getSetNumber());
            setBinding.loggedSetDetails.setText(setDetails(loggedSet));
            setBinding.loggedSetExp.setText(loggedSet.getExp() + " EXP");
            int setIndex = index;
            setBinding.getRoot().setOnClickListener(v -> {
                viewModel.removeSet(loggedExercise.getExercise().getId(), setIndex);
                Toast.makeText(requireContext(), "Set removed", Toast.LENGTH_SHORT).show();
            });
            itemBinding.setContainer.addView(setBinding.getRoot());
            index++;
        }
    }

    private void renderEditor(ItemWorkoutExerciseBinding itemBinding, Exercise exercise) {
        itemBinding.editorContainer.removeAllViews();
        ItemSetEditorBinding editorBinding = ItemSetEditorBinding.inflate(LayoutInflater.from(requireContext()), itemBinding.editorContainer, false);
        setupSetTypeSpinner(editorBinding);
        configureEditorFields(editorBinding, exercise.getExerciseType());
        editorBinding.saveSetButton.setOnClickListener(v -> {
            viewModel.addSet(
                    exercise.getId(),
                    selectedSetType(editorBinding),
                    optionalInt(editorBinding.repsInput),
                    optionalDouble(editorBinding.weightInput),
                    optionalInt(editorBinding.durationInput),
                    optionalDouble(editorBinding.distanceInput),
                    optionalDouble(editorBinding.assistanceInput),
                    editorBinding.progressiveOverloadCheckBox.isChecked()
            );
            editingExerciseId = -1L;
        });
        itemBinding.editorContainer.addView(editorBinding.getRoot());
    }

    private void setupSetTypeSpinner(ItemSetEditorBinding editorBinding) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, setTypeLabels());
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        editorBinding.setTypeSpinner.setAdapter(adapter);
    }

    private void configureEditorFields(ItemSetEditorBinding editorBinding, ExerciseType type) {
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

        editorBinding.repsInput.setVisibility(reps ? View.VISIBLE : View.GONE);
        editorBinding.weightInput.setVisibility(weight ? View.VISIBLE : View.GONE);
        editorBinding.durationInput.setVisibility(duration ? View.VISIBLE : View.GONE);
        editorBinding.distanceInput.setVisibility(distance ? View.VISIBLE : View.GONE);
        editorBinding.assistanceInput.setVisibility(assistance ? View.VISIBLE : View.GONE);
    }

    private void renderReviewExercises() {
        if (binding == null) {
            return;
        }
        binding.reviewExerciseContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (LoggedExercise loggedExercise : latestLoggedExercises) {
            ItemWorkoutExerciseBinding itemBinding = ItemWorkoutExerciseBinding.inflate(inflater, binding.reviewExerciseContainer, false);
            itemBinding.exerciseName.setText(loggedExercise.getExercise().getName());
            itemBinding.exerciseType.setText(pretty(loggedExercise.getExercise().getExerciseType().name()));
            itemBinding.addSetButton.setVisibility(View.GONE);
            renderSets(itemBinding, loggedExercise);
            binding.reviewExerciseContainer.addView(itemBinding.getRoot());
        }
    }

    private void finishWorkout() {
        if (viewModel.setCount() == 0) {
            Toast.makeText(requireContext(), R.string.workout_no_sets, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.finishWorkout();
    }

    private void showWorkout() {
        binding.workoutContent.setVisibility(View.VISIBLE);
        binding.pickerContent.setVisibility(View.GONE);
        binding.reviewContent.setVisibility(View.GONE);
    }

    private void showPicker() {
        binding.workoutContent.setVisibility(View.GONE);
        binding.pickerContent.setVisibility(View.VISIBLE);
        binding.reviewContent.setVisibility(View.GONE);
    }

    private void showReview() {
        renderReviewExercises();
        updateReviewSummary();
        binding.workoutContent.setVisibility(View.GONE);
        binding.pickerContent.setVisibility(View.GONE);
        binding.reviewContent.setVisibility(View.VISIBLE);
    }

    private void updateTimer() {
        if (binding == null) {
            return;
        }
        binding.timerValue.setText(formatElapsed(elapsedSeconds()));
        updateReviewSummary();
    }

    private void updateReviewSummary() {
        if (binding == null) {
            return;
        }
        binding.reviewSummaryValue.setText(getString(
                R.string.workout_review_summary,
                latestLoggedExercises.size(),
                viewModel.setCount(),
                lastTotalExp,
                formatElapsed(elapsedSeconds())
        ));
    }

    private String primaryAndSecondary(Exercise exercise) {
        String primary = pretty(exercise.getPrimaryMuscleGroup().name());
        String secondary = exercise.getSecondaryMuscleGroups();
        if (secondary == null || secondary.trim().isEmpty()) {
            return primary;
        }
        return primary + " + " + pretty(secondary);
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

    private SetType selectedSetType(ItemSetEditorBinding editorBinding) {
        int position = editorBinding.setTypeSpinner.getSelectedItemPosition();
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
