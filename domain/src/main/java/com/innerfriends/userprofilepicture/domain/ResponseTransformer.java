package com.innerfriends.userprofilepicture.domain;

import java.util.List;

public interface ResponseTransformer<R> {

    R toResponse(ContentUserProfilePicture contentProfilePicture);

    R toResponse(UserProfilePictureSaved profilePictureSaved);

    R toResponse(UserProfilePictureIdentifier userProfilePictureIdentifiers);

    R toResponse(List<? extends UserProfilePictureIdentifier> profilePictureIdentifiers);

    R toResponse(UserProfilePictureNotAvailableYetException userProfilePictureNotAvailableYetException);

    R toResponse(UserProfilePictureVersionUnknownException userProfilePictureVersionUnknownException);

    R toResponse(UserProfilePictureRepositoryException userProfilePictureRepositoryException);

    R toResponse(Throwable throwable);

}
