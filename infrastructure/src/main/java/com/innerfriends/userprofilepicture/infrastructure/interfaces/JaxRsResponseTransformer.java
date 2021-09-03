package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.*;

import javax.ws.rs.core.Response;

public class JaxRsResponseTransformer implements ResponseTransformer<Response> {

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
    public Response toResponse(final ProfilePictureSaved profilePictureSaved) {
        return Response.created(null)
                .entity(new ProfilePictureSavedDTO(profilePictureSaved))
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
