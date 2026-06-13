package com.lalit.levelforge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.lalit.levelforge.data.local.entity.UserProfile;

@Dao
public interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE id = 1")
    LiveData<UserProfile> observeProfile();

    @Query("SELECT * FROM user_profiles WHERE id = 1")
    UserProfile getProfile();

    @Upsert
    void upsert(UserProfile userProfile);
}
