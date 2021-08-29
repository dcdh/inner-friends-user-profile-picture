package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.ProfilePicture;
import com.innerfriends.userprofilepicture.domain.ProfilePictureNotAvailableYetException;
import com.innerfriends.userprofilepicture.domain.ProfilePictureRepositoryException;
import com.innerfriends.userprofilepicture.domain.ResponseTransformer;

public interface GetLastUserProfilePictureResponseTransformer<R> extends ResponseTransformer<R> {

    R toResponse(ProfilePicture profilePicture);

    R toResponse(ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException);

    R toResponse(ProfilePictureRepositoryException profilePictureRepositoryException);

    R toResponse(Throwable throwable);

}
