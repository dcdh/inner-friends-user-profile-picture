package com.innerfriends.userprofilepicture.domain;

public interface ResponseTransformer<R> {

    R toResponse(ContentUserProfilePicture contentProfilePicture);

    R toResponse(UserProfilePictureSaved profilePictureSaved);

    R toResponse(UserProfilePictureIdentifier userProfilePictureIdentifiers);

    R toResponse(UserProfilePictures userProfilePictures);

    R toResponse(UserProfilePictureNotAvailableYetException userProfilePictureNotAvailableYetException);

    R toResponse(UserProfilePictureVersionUnknownException userProfilePictureVersionUnknownException);

    R toResponse(UserProfilePictureRepositoryException userProfilePictureRepositoryException);

    R toResponse(UserProfilPictureFeaturedRepositoryException userProfilPictureFeaturedRepositoryException);

    R toResponse(Throwable throwable);

}
