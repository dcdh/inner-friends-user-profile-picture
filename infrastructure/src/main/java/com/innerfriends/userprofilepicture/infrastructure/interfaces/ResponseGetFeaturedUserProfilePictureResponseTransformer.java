package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ProfilePicture;
import com.innerfriends.userprofilepicture.domain.ProfilePictureNotAvailableYetException;
import com.innerfriends.userprofilepicture.domain.ProfilePictureRepositoryException;
import com.innerfriends.userprofilepicture.domain.usecase.GetFeaturedUserProfilePictureResponseTransformer;

import javax.ws.rs.core.Response;

public class ResponseGetFeaturedUserProfilePictureResponseTransformer implements GetFeaturedUserProfilePictureResponseTransformer<Response> {

    @Override
    public Response toResponse(final ProfilePicture profilePicture) {
        return Response.ok(profilePicture.picture())
                .header("Content-Disposition",
                        String.format("attachment;filename=%s%s", profilePicture.userPseudo().pseudo(), profilePicture.mediaType().extension()))
                .header("Content-Type", profilePicture.mediaType().contentType())
                .header("Content-Length", profilePicture.contentLength())
                .header("versionId", profilePicture.versionId())
                .build();
    }

    @Override
    public Response toResponse(final ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException) {
        return Response.status(Response.Status.NOT_FOUND).build();
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
