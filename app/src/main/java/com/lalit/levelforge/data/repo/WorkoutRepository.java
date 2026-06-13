package com.lalit.levelforge.data.repo;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.WorkoutSetDao;
import com.lalit.levelforge.data.local.dao.WorkoutSessionDao;
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.local.entity.WorkoutSession;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WorkoutRepository {

    private final WorkoutSessionDao workoutSessionDao;
    private final WorkoutSetDao workoutSetDao;
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public WorkoutRepository(WorkoutSessionDao workoutSessionDao, WorkoutSetDao workoutSetDao) {
        this.workoutSessionDao = workoutSessionDao;
        this.workoutSetDao = workoutSetDao;
    }

    public LiveData<List<WorkoutSession>> getRecentSessions() {
        return workoutSessionDao.getRecentSessions();
    }

    public LiveData<WorkoutSession> observeSession(long sessionId) {
        return workoutSessionDao.observeSession(sessionId);
    }

    public LiveData<List<WorkoutSet>> observeSetsForSession(long sessionId) {
        return workoutSetDao.observeSetsForSession(sessionId);
    }

    public void saveSession(WorkoutSession workoutSession) {
        diskExecutor.execute(() -> workoutSessionDao.upsert(workoutSession));
    }

    public void saveSet(WorkoutSet workoutSet) {
        diskExecutor.execute(() -> workoutSetDao.upsert(workoutSet));
    }
}
