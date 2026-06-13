package com.lalit.levelforge.ui.workout;

import android.os.Bundle;
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
    private final List<Exercise> exercises = new ArrayList<>();

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
        setupSetTypes();

        viewModel.getExercises().observe(getViewLifecycleOwner(), this::renderExerciseChoices);
        viewModel.getLoggedSets().observe(getViewLifecycleOwner(), this::renderLoggedSets);
        viewModel.getTotalExp().observe(getViewLifecycleOwner(), exp ->
                binding.previewExpValue.setText(getString(R.string.workout_preview_exp, exp == null ? 0 : exp)));
        viewModel.getSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved != null && saved) {
                Toast.makeText(requireContext(), R.string.workout_saved, Toast.LENGTH_SHORT).show();
                viewModel.consumeSaved();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        binding.addSetButton.setOnClickListener(v -> addSet());
        binding.postWorkoutButton.setOnClickListener(v -> postWorkout());
    }

    private void setupSetTypes() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, setTypeLabels());
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.setTypeSpinner.setAdapter(adapter);
    }

    private void renderExerciseChoices(List<Exercise> loadedExercises) {
        exercises.clear();
        if (loadedExercises != null) {
            exercises.addAll(loadedExercises);
        }

        List<String> labels = new ArrayList<>();
        for (Exercise exercise : exercises) {
            labels.add(exercise.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.exerciseSpinner.setAdapter(adapter);
    }

    private void addSet() {
        if (exercises.isEmpty()) {
            Toast.makeText(requireContext(), R.string.workout_missing_exercise, Toast.LENGTH_SHORT).show();
            return;
        }

        Exercise exercise = exercises.get(binding.exerciseSpinner.getSelectedItemPosition());
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
    }

    private void postWorkout() {
        Integer totalExp = viewModel.getTotalExp().getValue();
        if (totalExp == null || totalExp == 0) {
            Toast.makeText(requireContext(), R.string.workout_no_sets, Toast.LENGTH_SHORT).show();
            return;
        }

        String title = binding.workoutTitleInput.getText().toString().trim();
        if (title.isEmpty()) {
            title = "Strength workout";
        }
        viewModel.finishWorkout(title);
    }

    private void renderLoggedSets(List<LoggedSet> sets) {
        binding.loggedSetContainer.removeAllViews();
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

    private List<String> setTypeLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("Working set");
        labels.add("Warmup");
        labels.add("Failure");
        labels.add("AMRAP");
        return labels;
    }

    private int optionalInt(EditText editText) {
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    private double optionalDouble(EditText editText) {
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

    private String pretty(String value) {
        String text = value.replace('_', ' ').toLowerCase(Locale.US);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
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
