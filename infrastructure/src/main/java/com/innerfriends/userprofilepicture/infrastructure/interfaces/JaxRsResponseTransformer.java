package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.*;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class JaxRsResponseTransformer implements ResponseTransformer<Response> {

    @Override
    public Response toResponse(final ContentProfilePicture contentProfilePicture) {
        return Response.ok(contentProfilePicture.picture())
                .header("Content-Disposition",
                        String.format("attachment;filename=%s%s", contentProfilePicture.userPseudo().pseudo(), contentProfilePicture.mediaType().extension()))
                .header("Content-Type", contentProfilePicture.mediaType().contentType())
                .header("Content-Length", contentProfilePicture.contentLength())
                .header("versionId", contentProfilePicture.versionId().version())
                .build();
    }

    @Override
    public Response toResponse(final ProfilePictureSaved profilePictureSaved) {
        return Response.created(null)
                .entity(new ProfilePictureSavedDTO(profilePictureSaved))
                .build();
    }

    @Override
    public Response toResponse(final ProfilePictureIdentifier profilePictureIdentifiers) {
        return Response.ok(new ProfilePictureIdentifierDTO(profilePictureIdentifiers))
                .build();
    }

    @Override
    public Response toResponse(final List<? extends ProfilePictureIdentifier> profilePictureIdentifiers) {
        return Response.ok(
                profilePictureIdentifiers.stream()
                        .map(ProfilePictureIdentifierDTO::new)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public Response toResponse(final ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response toResponse(final ProfilePictureVersionUnknownException profilePictureVersionUnknownException) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response toResponse(final ProfilePictureRepositoryException profilePictureRepositoryException) {
        return Response.serverError().build();
    }

    @Override
    public Response toResponse(final Throwable throwable) {
        return Response.serverError().build();
    }

}
