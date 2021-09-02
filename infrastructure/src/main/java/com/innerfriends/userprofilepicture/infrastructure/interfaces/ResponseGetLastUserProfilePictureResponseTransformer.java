package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ProfilePicture;
import com.innerfriends.userprofilepicture.domain.ProfilePictureNotAvailableYetException;
import com.innerfriends.userprofilepicture.domain.ProfilePictureRepositoryException;
import com.innerfriends.userprofilepicture.domain.usecase.GetLastUserProfilePictureResponseTransformer;

import javax.ws.rs.core.Response;

public class ResponseGetLastUserProfilePictureResponseTransformer implements GetLastUserProfilePictureResponseTransformer<Response> {

    @Override
    public Response toResponse(final ProfilePicture profilePicture) {
        return Response.ok(profilePicture.picture())
                .header("Content-Disposition", "attachment;filename=" + profilePicture.userPseudo().pseudo())
                .header("Content-Type", profilePicture.mediaType().mimeType())
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