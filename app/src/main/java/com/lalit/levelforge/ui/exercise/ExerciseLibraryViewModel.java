package com.lalit.levelforge.ui.exercise;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.Exercise;
import com.lalit.levelforge.data.repo.ExerciseRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ExerciseLibraryViewModel extends ViewModel {

    private final LiveData<List<Exercise>> exercises;

    @Inject
    public ExerciseLibraryViewModel(ExerciseRepository exerciseRepository) {
        exerciseRepository.seedDefaultExercises();
        exercises = exerciseRepository.observeExercises();
    }

    public LiveData<List<Exercise>> getExercises() {
        return exercises;
    }
}
