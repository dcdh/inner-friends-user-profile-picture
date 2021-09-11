package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class ListUserProfilPicturesUseCase<R> implements UseCase<R, ListUserProfilPicturesCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;

    public ListUserProfilPicturesUseCase(final UserProfilePictureRepository userProfilePictureRepository,
                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
    }

    @Override
    public Uni<R> execute(final ListUserProfilPicturesCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> userProfilePictureCacheRepository.get(command.userPseudo()))
                .map(cachedUserProfilePictures -> responseTransformer.toResponse(cachedUserProfilePictures.profilePictureIdentifiers()))
                .onFailure()
                .recoverWithUni(() ->
                        Uni.createFrom()
                                .deferred(() -> userProfilePictureRepository.listByUserPseudo(command.userPseudo(), command.mediaType()))
                                .chain(profilePictureIdentifiers -> userProfilePictureCacheRepository.store(command.userPseudo(), profilePictureIdentifiers)
                                        .onItemOrFailure().transform((item, exception) -> profilePictureIdentifiers))
                                .map(profilePictureIdentifiers -> responseTransformer.toResponse(profilePictureIdentifiers))
                                .onFailure(UserProfilePictureRepositoryException.class)
                                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((UserProfilePictureRepositoryException) profilePictureRepositoryException))
                                .onFailure()
                                .recoverWithItem(exception -> responseTransformer.toResponse(exception))
                );
    }

}
