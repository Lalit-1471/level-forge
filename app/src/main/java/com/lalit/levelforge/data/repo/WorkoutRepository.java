package com.lalit.levelforge.data.repo;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.WorkoutSessionDao;
import com.lalit.levelforge.data.local.entity.WorkoutSession;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WorkoutRepository {

    private final WorkoutSessionDao workoutSessionDao;

    @Inject
    public WorkoutRepository(WorkoutSessionDao workoutSessionDao) {
        this.workoutSessionDao = workoutSessionDao;
    }

    public LiveData<List<WorkoutSession>> getRecentSessions() {
        return workoutSessionDao.getRecentSessions();
    }
}

