package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class UserProfilePictureVersionUnknownException extends RuntimeException {

    private final UserProfilePictureIdentifier userProfilePictureIdentifier;

    public UserProfilePictureVersionUnknownException(final UserProfilePictureIdentifier userProfilePictureIdentifier) {
        this.userProfilePictureIdentifier = Objects.requireNonNull(userProfilePictureIdentifier);
    }

    public UserProfilePictureIdentifier profilePictureIdentifier() {
        return userProfilePictureIdentifier;
    }
}
