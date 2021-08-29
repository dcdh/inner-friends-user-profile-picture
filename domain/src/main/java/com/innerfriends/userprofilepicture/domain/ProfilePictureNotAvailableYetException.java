package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class ProfilePictureNotAvailableYetException extends RuntimeException {

    private final UserPseudo userPseudo;

    public ProfilePictureNotAvailableYetException(final UserPseudo userPseudo) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
    }

    public UserPseudo userPseudo() {
        return userPseudo;
    }
}
