package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class ProfilePictureVersionUnknownException extends RuntimeException {

    private final UserProfilePictureIdentifier userProfilePictureIdentifier;

    public ProfilePictureVersionUnknownException(final UserProfilePictureIdentifier userProfilePictureIdentifier) {
        this.userProfilePictureIdentifier = Objects.requireNonNull(userProfilePictureIdentifier);
    }

    public UserProfilePictureIdentifier profilePictureIdentifier() {
        return userProfilePictureIdentifier;
    }
}
