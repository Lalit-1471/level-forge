package com.lalit.levelforge.ui.routine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.Routine;
import com.lalit.levelforge.data.repo.RoutineRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RoutinesViewModel extends ViewModel {

    private final LiveData<List<Routine>> routines;

    @Inject
    public RoutinesViewModel(RoutineRepository routineRepository) {
        routines = routineRepository.observeRoutines();
    }

    public LiveData<List<Routine>> getRoutines() {
        return routines;
    }
}
