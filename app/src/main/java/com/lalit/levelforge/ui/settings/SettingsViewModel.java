package com.lalit.levelforge.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.AppDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private final AppDatabase appDatabase;
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> resetComplete = new MutableLiveData<>(false);

    @Inject
    public SettingsViewModel(AppDatabase appDatabase) {
        this.appDatabase = appDatabase;
    }

    public LiveData<Boolean> getResetComplete() {
        return resetComplete;
    }

    public void resetLocalData() {
        diskExecutor.execute(() -> {
            appDatabase.clearAllTables();
            resetComplete.postValue(true);
        });
    }

    public void consumeResetComplete() {
        resetComplete.setValue(false);
    }
}
