package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public class UserProfileFeaturedNotInCacheException extends RuntimeException {

    private final UserPseudo userPseudo;

    public UserProfileFeaturedNotInCacheException(final UserPseudo userPseudo) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
    }

    public UserPseudo userPseudo() {
        return userPseudo;
    }
}
