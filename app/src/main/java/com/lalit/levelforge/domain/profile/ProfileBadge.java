package com.lalit.levelforge.domain.profile;

public class ProfileBadge {

    private final String title;
    private final String description;

    public ProfileBadge(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
