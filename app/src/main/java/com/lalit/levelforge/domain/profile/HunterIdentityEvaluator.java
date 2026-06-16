package com.lalit.levelforge.domain.profile;

import com.lalit.levelforge.data.local.entity.WorkoutSet;
import com.lalit.levelforge.data.local.entity.WorkoutSession;
import com.lalit.levelforge.data.local.relation.WorkoutSetWithExercise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HunterIdentityEvaluator {

    private HunterIdentityEvaluator() {
    }

    public static String behaviorTitleFor(List<WorkoutSession> sessions,
                                          List<WorkoutSetWithExercise> setDetails,
                                          String fallbackTitle) {
        int workouts = sessions == null ? 0 : sessions.size();
        BadgeStats stats = statsFor(setDetails);
        if (workouts >= 10 && stats.progressiveOverloads >= 10) {
            return "Relentless Hunter";
        }
        if (stats.weightPrs >= 3) {
            return "Iron Breaker";
        }
        if (stats.volumePrs >= 3) {
            return "Volume Forger";
        }
        if (stats.repsPrs >= 3) {
            return "Limit Breaker";
        }
        if (stats.sets >= 100) {
            return "Dungeon Grinder";
        }
        if (workouts >= 5) {
            return "Consistent Hunter";
        }
        if (stats.progressiveOverloads >= 1) {
            return "Awakened Hunter";
        }
        return fallbackTitle == null ? "Novice Hunter" : fallbackTitle;
    }

    public static List<ProfileBadge> collectedBadges(List<WorkoutSession> sessions,
                                                     List<WorkoutSetWithExercise> setDetails) {
        int workouts = sessions == null ? 0 : sessions.size();
        BadgeStats stats = statsFor(setDetails);
        List<ProfileBadge> badges = new ArrayList<>();
        if (workouts >= 1) {
            badges.add(new ProfileBadge("First Gate", "Posted your first workout."));
        }
        if (workouts >= 3) {
            badges.add(new ProfileBadge("Gate Regular", "Posted 3 workouts."));
        }
        if (workouts >= 10) {
            badges.add(new ProfileBadge("Dungeon Regular", "Posted 10 workouts."));
        }
        if (stats.sets >= 50) {
            badges.add(new ProfileBadge("Volume Smith", "Logged 50 completed sets."));
        }
        if (stats.sets >= 100) {
            badges.add(new ProfileBadge("Century Set", "Logged 100 completed sets."));
        }
        if (stats.progressiveOverloads >= 1) {
            badges.add(new ProfileBadge("Overload Spark", "Triggered your first progressive overload."));
        }
        if (stats.progressiveOverloads >= 10) {
            badges.add(new ProfileBadge("Overload Adept", "Triggered 10 progressive overloads."));
        }
        if (stats.weightPrs >= 3) {
            badges.add(new ProfileBadge("Iron Breaker", "Set 3 highest-weight PRs."));
        }
        if (stats.volumePrs >= 3) {
            badges.add(new ProfileBadge("Capacity Breaker", "Set 3 highest-volume PRs."));
        }
        if (stats.repsPrs >= 3) {
            badges.add(new ProfileBadge("Rep Limit Breaker", "Set 3 reps PRs."));
        }
        return Collections.unmodifiableList(badges);
    }

    private static BadgeStats statsFor(List<WorkoutSetWithExercise> setDetails) {
        BadgeStats stats = new BadgeStats();
        if (setDetails == null) {
            return stats;
        }
        for (WorkoutSetWithExercise detail : setDetails) {
            WorkoutSet set = detail.getWorkoutSet();
            if (set == null) {
                continue;
            }
            stats.sets++;
            if (set.isProgressiveOverload()) {
                stats.progressiveOverloads++;
            }
            if (set.isWeightPr()) {
                stats.weightPrs++;
            }
            if (set.isVolumePr()) {
                stats.volumePrs++;
            }
            if (set.isRepsPr()) {
                stats.repsPrs++;
            }
        }
        return stats;
    }

    private static class BadgeStats {
        private int sets;
        private int progressiveOverloads;
        private int weightPrs;
        private int volumePrs;
        private int repsPrs;
    }
}
