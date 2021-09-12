package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class UserProfileNotInCacheException extends RuntimeException {

    private final UserPseudo userPseudo;

    public UserProfileNotInCacheException(final UserPseudo userPseudo) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
    }

    public UserPseudo userPseudo() {
        return userPseudo;
    }
}
