package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ProfilePictureRepositoryException;
import com.innerfriends.userprofilepicture.domain.ProfilePictureSaved;
import com.innerfriends.userprofilepicture.domain.usecase.SaveUserProfilePictureResponseTransformer;

import javax.ws.rs.core.Response;

public class ResponseSaveUserProfilePictureResponseTransformer implements SaveUserProfilePictureResponseTransformer<Response> {

    @Override
    public Response toResponse(final ProfilePictureSaved profilePictureSaved) {
        return Response.created(null)
                .entity(new ProfilePictureSavedDTO(profilePictureSaved))
                .build();
    }

    @Override
    public Response toResponse(final ProfilePictureRepositoryException profilePictureRepositoryException) {
        profilePictureRepositoryException.printStackTrace();
        return Response.serverError().build();
    }

    @Override
    public Response toResponse(final Throwable throwable) {
        throwable.printStackTrace();
        return Response.serverError().build();
    }

}
