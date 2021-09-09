package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface UserProfilePictureCacheRepository {

    Uni<CachedUserProfilePicture> get(UserPseudo userPseudo) throws UserProfilePictureNotInCacheException;

    Uni<CachedUserProfilePicture> store(UserPseudo userPseudo, List<? extends ProfilePictureIdentifier> profilePictureIdentifier);

    Uni<CachedUserProfilePicture> storeFeatured(UserPseudo userPseudo, ProfilePictureIdentifier featured);

}
