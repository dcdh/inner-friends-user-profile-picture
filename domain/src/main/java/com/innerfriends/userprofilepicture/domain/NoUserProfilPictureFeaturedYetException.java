package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class NoUserProfilPictureFeaturedYetException extends RuntimeException {

    private final UserPseudo userPseudo;

    public NoUserProfilPictureFeaturedYetException(final UserPseudo userPseudo) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
    }

    public UserPseudo userPseudo() {
        return userPseudo;
    }
}
