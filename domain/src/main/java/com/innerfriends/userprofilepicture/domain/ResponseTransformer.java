package com.innerfriends.userprofilepicture.domain;

public interface ResponseTransformer<R> {

    R toResponse(ProfilePicture profilePicture);

    R toResponse(ProfilePictureSaved profilePictureSaved);

    R toResponse(ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException);

    R toResponse(ProfilePictureRepositoryException profilePictureRepositoryException);

    R toResponse(Throwable throwable);

}
