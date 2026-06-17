package com.lalit.levelforge.ui.routine;

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
import com.lalit.levelforge.data.local.entity.Routine;
import com.lalit.levelforge.databinding.FragmentRoutinesBinding;
import com.lalit.levelforge.databinding.ItemRoutineBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RoutinesFragment extends Fragment {

    public static final String ARG_ROUTINE_ID = "routineId";
    public static final String ARG_ROUTINE_BUILDER = "routineBuilder";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
    private FragmentRoutinesBinding binding;
    private RoutinesViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRoutinesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RoutinesViewModel.class);

        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        binding.createRoutineButton.setOnClickListener(v -> openRoutineBuilder(v));
        binding.emptyCreateRoutineButton.setOnClickListener(v -> openRoutineBuilder(v));
        viewModel.getRoutines().observe(getViewLifecycleOwner(), this::renderRoutines);
    }

    private void renderRoutines(List<Routine> routines) {
        List<Routine> safeRoutines = routines == null ? new ArrayList<>() : routines;
        binding.routineListContainer.removeAllViews();
        binding.emptyRoutinesState.setVisibility(safeRoutines.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Routine routine : safeRoutines) {
            ItemRoutineBinding itemBinding = ItemRoutineBinding.inflate(inflater, binding.routineListContainer, false);
            itemBinding.routineTitle.setText(titleFor(routine));
            itemBinding.routineMeta.setText(getString(
                    R.string.routines_updated_meta,
                    dateFormat.format(new Date(routine.getUpdatedAt()))
            ));
            String notes = routine.getNotes();
            boolean hasNotes = notes != null && !notes.trim().isEmpty();
            itemBinding.routineNotes.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
            itemBinding.routineNotes.setText(hasNotes ? notes.trim() : "");
            itemBinding.startRoutineButton.setOnClickListener(v -> startRoutine(v, routine.getId()));
            itemBinding.getRoot().setOnClickListener(v -> startRoutine(v, routine.getId()));
            binding.routineListContainer.addView(itemBinding.getRoot());
        }
    }

    private void openRoutineBuilder(View view) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_ROUTINE_BUILDER, true);
        Navigation.findNavController(view).navigate(R.id.action_routinesFragment_to_workoutLoggerFragment, args);
    }

    private void startRoutine(View view, long routineId) {
        Bundle args = new Bundle();
        args.putLong(ARG_ROUTINE_ID, routineId);
        Navigation.findNavController(view).navigate(R.id.action_routinesFragment_to_workoutLoggerFragment, args);
    }

    private String titleFor(Routine routine) {
        String title = routine.getTitle();
        if (title == null || title.trim().isEmpty()) {
            return getString(R.string.routines_default_title);
        }
        return title.trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
