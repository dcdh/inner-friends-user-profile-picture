package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

import java.util.List;

public interface UserProfilePictureRepository {

    Uni<UserProfilePictureSaved> save(UserPseudo userPseudo, byte[] picture, SupportedMediaType mediaType) throws UserProfilePictureRepositoryException;

    Uni<UserProfilePictureIdentifier> getLast(UserPseudo userPseudo, SupportedMediaType mediaType) throws UserProfilePictureNotAvailableYetException, UserProfilePictureRepositoryException;

    Uni<List<UserProfilePictureIdentifier>> listByUserPseudo(UserPseudo userPseudo, SupportedMediaType mediaType) throws UserProfilePictureRepositoryException;

    Uni<ContentUserProfilePicture> getContentByVersionId(UserProfilePictureIdentifier userProfilePictureIdentifier) throws UserProfilePictureVersionUnknownException, UserProfilePictureRepositoryException;

}
