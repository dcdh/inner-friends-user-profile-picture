package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class ListUserProfilPicturesUseCase<R> implements UseCase<R, ListUserProfilPicturesCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private final LockMechanism lockMechanism;

    public ListUserProfilPicturesUseCase(final UserProfilePictureRepository userProfilePictureRepository,
                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                         final LockMechanism lockMechanism) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
        this.lockMechanism = Objects.requireNonNull(lockMechanism);
    }

    @Override
    public Uni<R> execute(final ListUserProfilPicturesCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> lockMechanism.lock(command.userPseudo()))
                .chain(() -> userProfilePictureCacheRepository.get(command.userPseudo()))
                .map(cachedUserProfilePictures -> {
                    if (cachedUserProfilePictures.hasUserProfilePictureIdentifiersInCache()) {
                        return responseTransformer.toResponse(cachedUserProfilePictures.userProfilePictureIdentifiers());
                    }
                    throw new UserProfilePicturesNotInCacheException(command.userPseudo());
                })
                .onFailure()
                .recoverWithUni(() ->
                        Uni.createFrom()
                                .deferred(() -> userProfilePictureRepository.listByUserPseudo(command.userPseudo(), command.mediaType()))
                                .chain(userProfilePictureIdentifiers -> userProfilePictureCacheRepository.store(command.userPseudo(), userProfilePictureIdentifiers)
                                        .onItemOrFailure().transform((item, exception) -> userProfilePictureIdentifiers))
                                .map(profilePictureIdentifiers -> responseTransformer.toResponse(profilePictureIdentifiers))
                                .onFailure(UserProfilePictureRepositoryException.class)
                                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((UserProfilePictureRepositoryException) profilePictureRepositoryException))
                                .onFailure()
                                .recoverWithItem(exception -> responseTransformer.toResponse(exception))
                )
                .onTermination()
                .invoke(() -> lockMechanism.unlock(command.userPseudo()));
    }

}
