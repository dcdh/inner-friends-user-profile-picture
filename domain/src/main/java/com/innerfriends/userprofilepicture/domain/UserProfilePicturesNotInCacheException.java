package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class UserProfilePicturesNotInCacheException extends RuntimeException {

    private final UserPseudo userPseudo;

    public UserProfilePicturesNotInCacheException(final UserPseudo userPseudo) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
    }

    public UserPseudo userPseudo() {
        return userPseudo;
    }
}
