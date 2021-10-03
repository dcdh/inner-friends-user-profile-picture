package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

public interface UserProfilePictureCacheRepository {

    Uni<CachedUserProfilePictures> get(UserPseudo userPseudo) throws UserProfileNotInCacheException;

    Uni<CachedUserProfilePictures> store(UserPseudo userPseudo, UserProfilePictures userProfilePictures);

    Uni<CachedUserProfilePictures> storeFeatured(UserPseudo userPseudo, UserProfilePictureIdentifier featured);

    Uni<Void> evict(UserPseudo userPseudo);

}
