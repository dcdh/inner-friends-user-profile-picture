package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

public interface UserProfilPictureFeaturedRepository {

    Uni<UserProfilePictureIdentifier> getFeatured(UserPseudo userPseudo)
            throws NoUserProfilPictureFeaturedYetException, UserProfilPictureFeaturedRepositoryException;

    Uni<UserProfilePictureIdentifier> markAsFeatured(UserProfilePictureIdentifier profilePictureIdentifier)
            throws UserProfilPictureFeaturedRepositoryException;

}

