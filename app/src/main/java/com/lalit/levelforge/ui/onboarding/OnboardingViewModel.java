package com.lalit.levelforge.ui.onboarding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lalit.levelforge.data.local.entity.StrengthBaseline;
import com.lalit.levelforge.data.local.entity.UserGoal;
import com.lalit.levelforge.data.local.entity.UserProfile;
import com.lalit.levelforge.data.model.ActivityLevel;
import com.lalit.levelforge.data.model.Gender;
import com.lalit.levelforge.data.model.GoalType;
import com.lalit.levelforge.data.repo.ExerciseRepository;
import com.lalit.levelforge.data.repo.ProgressionRepository;
import com.lalit.levelforge.data.repo.UserProfileRepository;
import com.lalit.levelforge.domain.onboarding.NutritionRecommendation;
import com.lalit.levelforge.domain.onboarding.NutritionRecommendationCalculator;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OnboardingViewModel extends ViewModel {

    private final UserProfileRepository userProfileRepository;
    private final ProgressionRepository progressionRepository;
    private final ExerciseRepository exerciseRepository;
    private final MutableLiveData<NutritionRecommendation> recommendation = new MutableLiveData<>();

    @Inject
    public OnboardingViewModel(UserProfileRepository userProfileRepository,
                               ProgressionRepository progressionRepository,
                               ExerciseRepository exerciseRepository) {
        this.userProfileRepository = userProfileRepository;
        this.progressionRepository = progressionRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public LiveData<NutritionRecommendation> getRecommendation() {
        return recommendation;
    }

    public void completeOnboarding(UserProfile userProfile) {
        long now = System.currentTimeMillis();
        userProfile.setCreatedAt(now);
        userProfile.setUpdatedAt(now);
        userProfile.setOnboardingComplete(true);
        userProfile.setNutritionRecommendationShown(true);

        userProfileRepository.saveProfile(userProfile);
        progressionRepository.initializeLevelStateIfNeeded();
        exerciseRepository.seedDefaultExercises();
        recommendation.setValue(NutritionRecommendationCalculator.calculate(userProfile));
    }

    public UserProfile buildProfile(Gender gender, int age, double heightCm, double weightKg,
                                    GoalType goalType, double targetWeightKg, ActivityLevel activityLevel,
                                    String healthFlags, StrengthBaseline strengthBaseline) {
        UserProfile profile = new UserProfile();
        profile.setGender(gender);
        profile.setAge(age);
        profile.setHeightCm(heightCm);
        profile.setWeightKg(weightKg);
        profile.setHealthFlags(healthFlags);
        profile.setUserGoal(new UserGoal(goalType, targetWeightKg, activityLevel));
        profile.setStrengthBaseline(strengthBaseline);
        return profile;
    }
}
