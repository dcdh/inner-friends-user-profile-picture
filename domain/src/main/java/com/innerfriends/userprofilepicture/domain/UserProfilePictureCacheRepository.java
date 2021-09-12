package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface UserProfilePictureCacheRepository {

    Uni<CachedUserProfilePictures> get(UserPseudo userPseudo) throws UserProfileNotInCacheException;

    Uni<CachedUserProfilePictures> store(UserPseudo userPseudo, List<UserProfilePictureIdentifier> profilePictureIdentifier);

    Uni<CachedUserProfilePictures> storeFeatured(UserPseudo userPseudo, UserProfilePictureIdentifier featured);

    Uni<Void> evict(UserPseudo userPseudo);

}
