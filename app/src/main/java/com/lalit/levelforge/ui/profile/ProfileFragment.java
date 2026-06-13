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
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.databinding.FragmentProfileBinding;
import com.lalit.levelforge.databinding.ItemWorkoutSessionBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US);
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

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
    }

    private void renderWorkouts(List<WorkoutSession> sessions) {
        binding.workoutListContainer.removeAllViews();
        int workoutCount = sessions == null ? 0 : sessions.size();
        int totalExp = totalExp(sessions);
        binding.profileSummaryValue.setText(getString(R.string.profile_summary_value, workoutCount, totalExp));

        if (workoutCount == 0) {
            binding.emptyWorkoutsText.setVisibility(View.VISIBLE);
            return;
        }

        binding.emptyWorkoutsText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (WorkoutSession session : sessions) {
            ItemWorkoutSessionBinding itemBinding = ItemWorkoutSessionBinding.inflate(inflater, binding.workoutListContainer, false);
            itemBinding.workoutTitle.setText(session.getTitle());
            itemBinding.workoutMeta.setText(getString(
                    R.string.profile_workout_meta,
                    dateFormat.format(new Date(session.getCompletedAt())),
                    formatDuration(session.getDurationSeconds())
            ));
            itemBinding.workoutExp.setText(getString(R.string.profile_workout_exp, session.getTotalExp()));
            binding.workoutListContainer.addView(itemBinding.getRoot());
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
