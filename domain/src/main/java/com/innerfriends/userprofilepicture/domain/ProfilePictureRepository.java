package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface ProfilePictureRepository {

    Uni<ProfilePictureSaved> save(UserPseudo userPseudo, byte[] picture, SupportedMediaType mediaType) throws ProfilePictureRepositoryException;

    Uni<ProfilePictureIdentifier> getLast(UserPseudo userPseudo, SupportedMediaType mediaType) throws ProfilePictureNotAvailableYetException, ProfilePictureRepositoryException;

    Uni<List<ProfilePictureIdentifier>> listByUserPseudo(UserPseudo userPseudo, SupportedMediaType mediaType) throws ProfilePictureRepositoryException;

    Uni<ContentProfilePicture> getContentByVersionId(ProfilePictureIdentifier profilePictureIdentifier) throws ProfilePictureVersionUnknownException, ProfilePictureRepositoryException;

}
