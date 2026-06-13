package com.lalit.levelforge.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.lalit.levelforge.R;
import com.lalit.levelforge.databinding.FragmentHomeBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getSubtitle().observe(getViewLifecycleOwner(), value -> binding.subtitleText.setText(value));
        viewModel.getLevelValue().observe(getViewLifecycleOwner(), value -> binding.levelValue.setText(value));
        viewModel.getExpValue().observe(getViewLifecycleOwner(), value -> binding.expValue.setText(value));
        viewModel.getDailyTask().observe(getViewLifecycleOwner(), value -> binding.dailyQuestValue.setText(value));
        viewModel.getWeeklySummary().observe(getViewLifecycleOwner(), value -> binding.weeklySummaryValue.setText(value));
        viewModel.isOnboardingComplete().observe(getViewLifecycleOwner(), complete -> {
            boolean isComplete = complete != null && complete;
            binding.onboardingButton.setText(isComplete ? R.string.dashboard_edit_profile : R.string.dashboard_start_onboarding);
        });

        binding.onboardingButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_onboardingFragment));
        binding.exerciseLibraryButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_exerciseLibraryFragment));
        binding.logWorkoutButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_workoutLoggerFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
