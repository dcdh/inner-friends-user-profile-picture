package com.innerfriends.userprofilepicture.domain;

import java.util.List;

public interface CachedUserProfilePictures {

    UserPseudo userPseudo();

    List<? extends UserProfilePictureIdentifier> userProfilePictureIdentifiers();

    UserProfilePictureIdentifier featured();

    default boolean hasFeaturedInCache() {
        return featured() != null;
    }

    default boolean hasUserProfilePictureIdentifiersInCache() {
        return userProfilePictureIdentifiers() != null;
    }

}
