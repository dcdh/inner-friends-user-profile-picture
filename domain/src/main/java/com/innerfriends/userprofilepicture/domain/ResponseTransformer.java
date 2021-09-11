package com.innerfriends.userprofilepicture.domain;

import java.util.List;

public interface ResponseTransformer<R> {

    R toResponse(ContentUserProfilePicture contentProfilePicture);

    R toResponse(UserProfilePictureSaved profilePictureSaved);

    R toResponse(UserProfilePictureIdentifier userProfilePictureIdentifiers);

    R toResponse(List<? extends UserProfilePictureIdentifier> profilePictureIdentifiers);

    R toResponse(ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException);

    R toResponse(ProfilePictureVersionUnknownException profilePictureVersionUnknownException);

    R toResponse(ProfilePictureRepositoryException profilePictureRepositoryException);

    R toResponse(Throwable throwable);

}
