package com.lalit.levelforge.data.repo;

import android.os.Handler;
import android.os.Looper;

import com.lalit.levelforge.data.local.dao.ExpEventDao;
import com.lalit.levelforge.data.local.dao.LevelStateDao;
import com.lalit.levelforge.data.local.dao.WorkoutSessionDao;
import com.lalit.levelforge.data.local.dao.WorkoutSetDao;
import com.lalit.levelforge.data.local.entity.ExpEvent;
import com.lalit.levelforge.data.local.entity.LevelState;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.model.ExpSourceType;
import com.lalit.levelforge.data.model.RankTier;
import com.lalit.levelforge.domain.progression.LevelCurve;
import com.lalit.levelforge.domain.progression.RankEvaluator;
import com.lalit.levelforge.domain.progression.TitleCatalog;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WorkoutLogRepository {

    public interface SaveCallback {
        void onSaved(long sessionId, int sessionExp);
    }

    public interface ExerciseSetsCallback {
        void onLoaded(long exerciseId, List<WorkoutSet> sets);
    }

    private final WorkoutSessionDao workoutSessionDao;
    private final WorkoutSetDao workoutSetDao;
    private final ExpEventDao expEventDao;
    private final LevelStateDao levelStateDao;
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Inject
    public WorkoutLogRepository(WorkoutSessionDao workoutSessionDao,
                                WorkoutSetDao workoutSetDao,
                                ExpEventDao expEventDao,
                                LevelStateDao levelStateDao) {
        this.workoutSessionDao = workoutSessionDao;
        this.workoutSetDao = workoutSetDao;
        this.expEventDao = expEventDao;
        this.levelStateDao = levelStateDao;
    }

    public void saveCompletedWorkout(String title, List<WorkoutSet> sets, int totalExp,
                                     int durationSeconds, SaveCallback callback) {
        diskExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            WorkoutSession session = new WorkoutSession();
            session.setTitle(title);
            session.setStartedAt(now - (Math.max(0, durationSeconds) * 1000L));
            session.setCompletedAt(now);
            session.setCreatedAt(now);
            session.setUpdatedAt(now);
            session.setCompleted(true);
            session.setDurationSeconds(durationSeconds);
            session.setTotalExp(totalExp);

            long sessionId = workoutSessionDao.insert(session);
            for (WorkoutSet set : sets) {
                set.setSessionId(sessionId);
                set.setCompleted(true);
                workoutSetDao.insert(set);
            }

            expEventDao.insert(new ExpEvent(
                    ExpSourceType.WORKOUT,
                    sessionId,
                    totalExp,
                    title,
                    now
            ));
            updateLevelState(expEventDao.getTotalExp(), now);

            if (callback != null) {
                mainHandler.post(() -> callback.onSaved(sessionId, totalExp));
            }
        });
    }

    public void getCompletedSetsForExercise(long exerciseId, ExerciseSetsCallback callback) {
        diskExecutor.execute(() -> {
            List<WorkoutSet> sets = workoutSetDao.getCompletedSetsForExercise(exerciseId);
            if (callback != null) {
                mainHandler.post(() -> callback.onLoaded(exerciseId, sets));
            }
        });
    }

    private void updateLevelState(int totalExp, long now) {
        int level = LevelCurve.levelForTotalExp(totalExp);
        RankTier rankTier = RankEvaluator.rankForLevel(level);
        LevelState state = new LevelState(level, totalExp, rankTier, TitleCatalog.titleFor(level, rankTier), now);
        levelStateDao.upsert(state);
    }
}
