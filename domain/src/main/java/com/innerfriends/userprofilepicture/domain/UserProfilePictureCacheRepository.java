package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface UserProfilePictureCacheRepository {

    Uni<CachedUserProfilePictures> get(UserPseudo userPseudo) throws UserProfilePictureNotInCacheException;

    Uni<CachedUserProfilePictures> store(UserPseudo userPseudo, List<? extends ProfilePictureIdentifier> profilePictureIdentifier);

    Uni<CachedUserProfilePictures> storeFeatured(UserPseudo userPseudo, ProfilePictureIdentifier featured);

}
