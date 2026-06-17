package com.lalit.levelforge.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.lalit.levelforge.R;
import com.lalit.levelforge.databinding.FragmentSettingsBinding;

import java.util.Arrays;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    public static final String PREFS_NAME = "level_forge_settings";
    public static final String KEY_UNIT_SYSTEM = "unit_system";
    public static final String KEY_WEEK_START = "week_start";
    public static final String KEY_DEFAULT_REST_TIMER_SECONDS = "default_rest_timer_seconds";

    private static final String UNIT_KG = "kg";
    private static final String UNIT_LB = "lb";
    private static final String WEEK_MONDAY = "monday";
    private static final String WEEK_SUNDAY = "sunday";
    private static final int DEFAULT_REST_SECONDS = 90;

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;
    private SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupSpinners();
        renderSavedSettings();
        binding.backButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        binding.saveSettingsButton.setOnClickListener(v -> saveSettings());
        binding.resetDataButton.setOnClickListener(v -> confirmReset());

        viewModel.getResetComplete().observe(getViewLifecycleOwner(), complete -> {
            if (complete != null && complete) {
                Toast.makeText(requireContext(), R.string.settings_reset_done, Toast.LENGTH_SHORT).show();
                viewModel.consumeResetComplete();
                Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
            }
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner,
                Arrays.asList(getString(R.string.settings_unit_kg), getString(R.string.settings_unit_lb))
        );
        unitAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.unitSpinner.setAdapter(unitAdapter);

        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_spinner,
                Arrays.asList(getString(R.string.settings_week_monday), getString(R.string.settings_week_sunday))
        );
        weekAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        binding.weekStartSpinner.setAdapter(weekAdapter);
    }

    private void renderSavedSettings() {
        String unit = preferences.getString(KEY_UNIT_SYSTEM, UNIT_KG);
        String weekStart = preferences.getString(KEY_WEEK_START, WEEK_MONDAY);
        int restSeconds = preferences.getInt(KEY_DEFAULT_REST_TIMER_SECONDS, DEFAULT_REST_SECONDS);

        binding.unitSpinner.setSelection(UNIT_LB.equals(unit) ? 1 : 0);
        binding.weekStartSpinner.setSelection(WEEK_SUNDAY.equals(weekStart) ? 1 : 0);
        binding.restTimerInput.setText(String.valueOf(restSeconds));
    }

    private void saveSettings() {
        int restSeconds = optionalInt(binding.restTimerInput.getText().toString(), DEFAULT_REST_SECONDS);
        preferences.edit()
                .putString(KEY_UNIT_SYSTEM, binding.unitSpinner.getSelectedItemPosition() == 1 ? UNIT_LB : UNIT_KG)
                .putString(KEY_WEEK_START, binding.weekStartSpinner.getSelectedItemPosition() == 1 ? WEEK_SUNDAY : WEEK_MONDAY)
                .putInt(KEY_DEFAULT_REST_TIMER_SECONDS, Math.max(0, restSeconds))
                .apply();
        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_reset_title)
                .setMessage(R.string.settings_reset_message)
                .setNegativeButton(R.string.settings_reset_cancel, null)
                .setPositiveButton(R.string.settings_reset_confirm, (dialog, which) -> viewModel.resetLocalData())
                .show();
    }

    private int optionalInt(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
