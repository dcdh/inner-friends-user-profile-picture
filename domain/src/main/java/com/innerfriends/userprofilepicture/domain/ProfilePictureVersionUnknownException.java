package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class ProfilePictureVersionUnknownException extends RuntimeException {

    private final ProfilePictureIdentifier profilePictureIdentifier;

    public ProfilePictureVersionUnknownException(final ProfilePictureIdentifier profilePictureIdentifier) {
        this.profilePictureIdentifier = Objects.requireNonNull(profilePictureIdentifier);
    }

    public ProfilePictureIdentifier profilePictureIdentifier() {
        return profilePictureIdentifier;
    }
}
