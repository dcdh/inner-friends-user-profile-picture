package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface UserProfilePictureRepository {

    Uni<UserProfilePictureSaved> save(UserPseudo userPseudo, byte[] picture, SupportedMediaType mediaType) throws ProfilePictureRepositoryException;

    Uni<UserProfilePictureIdentifier> getLast(UserPseudo userPseudo, SupportedMediaType mediaType) throws ProfilePictureNotAvailableYetException, ProfilePictureRepositoryException;

    Uni<List<? extends UserProfilePictureIdentifier>> listByUserPseudo(UserPseudo userPseudo, SupportedMediaType mediaType) throws ProfilePictureRepositoryException;

    Uni<ContentUserProfilePicture> getContentByVersionId(UserProfilePictureIdentifier userProfilePictureIdentifier) throws ProfilePictureVersionUnknownException, ProfilePictureRepositoryException;

}
