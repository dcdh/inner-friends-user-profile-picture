package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.ProfilePictureRepositoryException;
import com.innerfriends.userprofilepicture.domain.ProfilePictureSaved;
import com.innerfriends.userprofilepicture.domain.ResponseTransformer;

public interface SaveUserProfilePictureResponseTransformer<R> extends ResponseTransformer<R> {

    R toResponse(ProfilePictureSaved profilePictureSaved);

    R toResponse(ProfilePictureRepositoryException profilePictureRepositoryException);

    R toResponse(Throwable throwable);

}
