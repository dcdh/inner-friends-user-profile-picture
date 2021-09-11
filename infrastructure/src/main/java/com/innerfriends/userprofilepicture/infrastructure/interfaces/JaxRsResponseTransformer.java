package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.*;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class JaxRsResponseTransformer implements ResponseTransformer<Response> {

    @Override
    public Response toResponse(final ContentUserProfilePicture contentUserProfilePicture) {
        return Response.ok(contentUserProfilePicture.picture())
                .header("Content-Disposition",
                        String.format("attachment;filename=%s%s", contentUserProfilePicture.userPseudo().pseudo(), contentUserProfilePicture.mediaType().extension()))
                .header("Content-Type", contentUserProfilePicture.mediaType().contentType())
                .header("Content-Length", contentUserProfilePicture.contentLength())
                .header("versionId", contentUserProfilePicture.versionId().version())
                .build();
    }

    @Override
    public Response toResponse(final UserProfilePictureSaved profilePictureSaved) {
        return Response.created(null)
                .entity(new UserProfilePictureSavedDTO(profilePictureSaved))
                .build();
    }

    @Override
    public Response toResponse(final UserProfilePictureIdentifier userProfilePictureIdentifiers) {
        return Response.ok(new UserProfilePictureIdentifierDTO(userProfilePictureIdentifiers))
                .build();
    }

    @Override
    public Response toResponse(final List<? extends UserProfilePictureIdentifier> userProfilePictureIdentifiers) {
        return Response.ok(
                userProfilePictureIdentifiers.stream()
                        .map(UserProfilePictureIdentifierDTO::new)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public Response toResponse(final UserProfilePictureNotAvailableYetException userProfilePictureNotAvailableYetException) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response toResponse(final UserProfilePictureVersionUnknownException userProfilePictureVersionUnknownException) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response toResponse(final UserProfilePictureRepositoryException userProfilePictureRepositoryException) {
        return Response.serverError().build();
    }

    @Override
    public Response toResponse(final Throwable throwable) {
        return Response.serverError().build();
    }

}
