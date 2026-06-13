package com.lalit.levelforge.ui.exercise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.databinding.FragmentExerciseLibraryBinding;
import com.lalit.levelforge.databinding.ItemExerciseBinding;

import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ExerciseLibraryFragment extends Fragment {

    private FragmentExerciseLibraryBinding binding;
    private ExerciseLibraryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExerciseLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ExerciseLibraryViewModel.class);
        viewModel.getExercises().observe(getViewLifecycleOwner(), this::renderExercises);
    }

    private void renderExercises(List<Exercise> exercises) {
        binding.exerciseListContainer.removeAllViews();
        if (exercises == null || exercises.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
            return;
        }
        binding.emptyState.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Exercise exercise : exercises) {
            ItemExerciseBinding itemBinding = ItemExerciseBinding.inflate(inflater, binding.exerciseListContainer, false);
            itemBinding.exerciseName.setText(exercise.getName());
            itemBinding.exerciseType.setText(pretty(exercise.getExerciseType().name()));
            itemBinding.exerciseMuscles.setText(primaryAndSecondary(exercise));
            itemBinding.exerciseExp.setText(exercise.getBaseExp() + " base EXP");
            binding.exerciseListContainer.addView(itemBinding.getRoot());
        }
    }

    private String primaryAndSecondary(Exercise exercise) {
        String primary = pretty(exercise.getPrimaryMuscleGroup().name());
        String secondary = exercise.getSecondaryMuscleGroups();
        if (secondary == null || secondary.trim().isEmpty()) {
            return primary;
        }
        return primary + " + " + pretty(secondary);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
