package com.lalit.levelforge.data.repo;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.ExpEventDao;
import com.lalit.levelforge.data.local.dao.LevelStateDao;
import com.lalit.levelforge.data.local.entity.ExpEvent;
import com.lalit.levelforge.data.local.entity.LevelState;
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
public class ProgressionRepository {

    private final ExpEventDao expEventDao;
    private final LevelStateDao levelStateDao;
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public ProgressionRepository(ExpEventDao expEventDao, LevelStateDao levelStateDao) {
        this.expEventDao = expEventDao;
        this.levelStateDao = levelStateDao;
    }

    public LiveData<LevelState> observeLevelState() {
        return levelStateDao.observeLevelState();
    }

    public LiveData<List<ExpEvent>> observeRecentExpEvents(int limit) {
        return expEventDao.observeRecentEvents(limit);
    }

    public void initializeLevelStateIfNeeded() {
        diskExecutor.execute(() -> {
            if (levelStateDao.getLevelState() != null) {
                return;
            }
            RankTier rankTier = RankEvaluator.rankForLevel(1);
            LevelState state = new LevelState(1, 0, rankTier, TitleCatalog.titleFor(1, rankTier), System.currentTimeMillis());
            levelStateDao.upsert(state);
        });
    }

    public void saveLevelStateFromTotalExp(int totalExp) {
        diskExecutor.execute(() -> {
            int level = LevelCurve.levelForTotalExp(totalExp);
            RankTier rankTier = RankEvaluator.rankForLevel(level);
            LevelState state = new LevelState(level, totalExp, rankTier, TitleCatalog.titleFor(level, rankTier), System.currentTimeMillis());
            levelStateDao.upsert(state);
        });
    }
}
