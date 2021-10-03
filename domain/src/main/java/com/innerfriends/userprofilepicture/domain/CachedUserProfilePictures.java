package com.innerfriends.userprofilepicture.domain;

import java.util.List;
import java.util.stream.Collectors;

public interface CachedUserProfilePictures extends UserProfilePictures {

    UserPseudo userPseudo();

    List<? extends UserProfilePictureIdentifier> userProfilePictureIdentifiers();

    UserProfilePictureIdentifier featured();

    default boolean hasFeaturedInCache() {
        return featured() != null;
    }

    default boolean hasUserProfilePictureIdentifiersInCache() {
        return userProfilePictureIdentifiers() != null;
    }

    @Override
    default List<DomainUserProfilePicture> userProfilePictures() {
        return userProfilePictureIdentifiers()
                .stream()
                .map(userProfilePictureIdentifier -> new DomainUserProfilePicture(userProfilePictureIdentifier, featured()))
                .collect(Collectors.toList());
    }

}
