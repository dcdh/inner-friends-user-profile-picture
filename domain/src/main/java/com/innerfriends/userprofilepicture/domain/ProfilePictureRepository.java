package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

public interface ProfilePictureRepository {

    Uni<ProfilePictureSaved> save(UserPseudo userPseudo, byte[] picture, SupportedMediaType mediaType) throws ProfilePictureRepositoryException;

    Uni<ProfilePicture> getLast(UserPseudo userPseudo, SupportedMediaType mediaType) throws ProfilePictureNotAvailableYetException, ProfilePictureRepositoryException;

}
