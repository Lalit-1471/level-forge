package com.lalit.levelforge.ui.onboarding;

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
import com.lalit.levelforge.data.local.entity.StrengthBaseline;
import com.lalit.levelforge.data.local.entity.UserProfile;
import com.lalit.levelforge.data.model.ActivityLevel;
import com.lalit.levelforge.data.model.Gender;
import com.lalit.levelforge.data.model.GoalType;
import com.lalit.levelforge.databinding.FragmentOnboardingBinding;
import com.lalit.levelforge.domain.onboarding.NutritionRecommendation;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OnboardingFragment extends Fragment {

    private FragmentOnboardingBinding binding;
    private OnboardingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        setupSpinners();

        binding.calculateButton.setOnClickListener(v -> submitProfile());
        binding.startButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        viewModel.getRecommendation().observe(getViewLifecycleOwner(), this::showRecommendation);
    }

    private void setupSpinners() {
        binding.genderSpinner.setAdapter(adapter("Male", "Female", "Non-binary", "Prefer not to say"));
        binding.goalSpinner.setAdapter(adapter("Build muscle", "Lose weight", "Boost appearance"));
        binding.activitySpinner.setAdapter(adapter("Sedentary", "Lightly active", "Moderately active", "Very active"));
        binding.activitySpinner.setSelection(2);
    }

    private ArrayAdapter<String> adapter(String... values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner, values);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        return adapter;
    }

    private void submitProfile() {
        try {
            UserProfile profile = viewModel.buildProfile(
                    selectedGender(),
                    requiredInt(binding.ageInput),
                    requiredDouble(binding.heightInput),
                    requiredDouble(binding.weightInput),
                    selectedGoal(),
                    requiredDouble(binding.targetWeightInput),
                    selectedActivityLevel(),
                    selectedHealthFlags(),
                    buildStrengthBaseline()
            );
            viewModel.completeOnboarding(profile);
        } catch (IllegalArgumentException exception) {
            Toast.makeText(requireContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private StrengthBaseline buildStrengthBaseline() {
        StrengthBaseline baseline = new StrengthBaseline();
        baseline.setMaxPushups(optionalInt(binding.maxPushupsInput));
        baseline.setMaxPullups(optionalInt(binding.maxPullupsInput));
        baseline.setMaxBodyweightSquats(optionalInt(binding.maxSquatsInput));
        baseline.setSquatKg(optionalDouble(binding.squatKgInput));
        baseline.setBenchKg(optionalDouble(binding.benchKgInput));
        baseline.setDeadliftKg(optionalDouble(binding.deadliftKgInput));
        baseline.setOverheadPressKg(optionalDouble(binding.overheadPressKgInput));
        return baseline;
    }

    private void showRecommendation(NutritionRecommendation recommendation) {
        binding.recommendationCard.setVisibility(View.VISIBLE);
        binding.caloriesValue.setText(getString(R.string.onboarding_calories_value, recommendation.getCalories()));
        binding.macrosValue.setText(getString(
                R.string.onboarding_macros_value,
                recommendation.getProteinGrams(),
                recommendation.getCarbsGrams(),
                recommendation.getFatGrams()
        ));
        binding.hydrationValue.setText(getString(R.string.onboarding_hydration_value, recommendation.getWaterLiters(), recommendation.getFiberGrams()));
        binding.workoutDurationValue.setText(getString(R.string.onboarding_workout_duration_value, recommendation.getWorkoutDurationMinutes()));
    }

    private Gender selectedGender() {
        int position = binding.genderSpinner.getSelectedItemPosition();
        if (position == 1) {
            return Gender.FEMALE;
        }
        if (position == 2) {
            return Gender.NON_BINARY;
        }
        if (position == 3) {
            return Gender.PREFER_NOT_TO_SAY;
        }
        return Gender.MALE;
    }

    private GoalType selectedGoal() {
        int position = binding.goalSpinner.getSelectedItemPosition();
        if (position == 1) {
            return GoalType.LOSE_WEIGHT;
        }
        if (position == 2) {
            return GoalType.BOOST_APPEARANCE;
        }
        return GoalType.BUILD_MUSCLE;
    }

    private ActivityLevel selectedActivityLevel() {
        int position = binding.activitySpinner.getSelectedItemPosition();
        if (position == 0) {
            return ActivityLevel.SEDENTARY;
        }
        if (position == 1) {
            return ActivityLevel.LIGHTLY_ACTIVE;
        }
        if (position == 3) {
            return ActivityLevel.VERY_ACTIVE;
        }
        return ActivityLevel.MODERATELY_ACTIVE;
    }

    private String selectedHealthFlags() {
        List<String> flags = new ArrayList<>();
        if (binding.jointPainCheckBox.isChecked()) {
            flags.add("JOINT_PAIN");
        }
        if (binding.backPainCheckBox.isChecked()) {
            flags.add("BACK_PAIN");
        }
        if (binding.heartConditionCheckBox.isChecked()) {
            flags.add("HEART_CONDITION");
        }
        if (binding.diabetesCheckBox.isChecked()) {
            flags.add("DIABETES");
        }
        if (binding.injuryCheckBox.isChecked()) {
            flags.add("INJURY");
        }
        return flags.isEmpty() ? "NONE" : String.join(",", flags);
    }

    private int requiredInt(EditText editText) {
        return (int) requiredDouble(editText);
    }

    private int optionalInt(EditText editText) {
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    private double requiredDouble(EditText editText) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(getString(R.string.onboarding_missing_required));
        }
        return Double.parseDouble(value);
    }

    private double optionalDouble(EditText editText) {
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? 0 : Double.parseDouble(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
