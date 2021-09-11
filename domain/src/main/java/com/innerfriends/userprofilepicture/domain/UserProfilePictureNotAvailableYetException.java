package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class UserProfilePictureNotAvailableYetException extends RuntimeException {

    private final UserPseudo userPseudo;

    public UserProfilePictureNotAvailableYetException(final UserPseudo userPseudo) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
    }

    public UserPseudo userPseudo() {
        return userPseudo;
    }
}
