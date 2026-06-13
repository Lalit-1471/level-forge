package com.lalit.levelforge.data.repo;

import androidx.lifecycle.LiveData;

import com.lalit.levelforge.data.local.dao.UserProfileDao;
import com.lalit.levelforge.data.local.entity.UserProfile;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserProfileRepository {

    private final UserProfileDao userProfileDao;
    private final Executor diskExecutor = Executors.newSingleThreadExecutor();

    @Inject
    public UserProfileRepository(UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
    }

    public LiveData<UserProfile> observeProfile() {
        return userProfileDao.observeProfile();
    }

    public void saveProfile(UserProfile userProfile) {
        diskExecutor.execute(() -> userProfileDao.upsert(userProfile));
    }
}
