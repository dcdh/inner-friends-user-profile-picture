package com.innerfriends.userprofilepicture.domain;

import java.util.List;

public interface ResponseTransformer<R> {

    R toResponse(ContentProfilePicture contentProfilePicture);

    R toResponse(ProfilePictureSaved profilePictureSaved);

    R toResponse(ProfilePictureIdentifier profilePictureIdentifiers);

    R toResponse(List<? extends ProfilePictureIdentifier> profilePictureIdentifiers);

    R toResponse(ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException);

    R toResponse(ProfilePictureVersionUnknownException profilePictureVersionUnknownException);

    R toResponse(ProfilePictureRepositoryException profilePictureRepositoryException);

    R toResponse(Throwable throwable);

}
