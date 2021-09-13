package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class GetFeaturedUserProfilePictureUseCase<R> implements UseCase<R, GetFeaturedUserProfilePictureCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private final LockMechanism lockMechanism;

    public GetFeaturedUserProfilePictureUseCase(final UserProfilePictureRepository userProfilePictureRepository,
                                                final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                                final LockMechanism lockMechanism) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
        this.lockMechanism = Objects.requireNonNull(lockMechanism);
    }

    @Override
    public Uni<R> execute(final GetFeaturedUserProfilePictureCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> lockMechanism.lock(command.userPseudo()))
                .chain(() -> userProfilePictureCacheRepository.get(command.userPseudo()))
                .map(cachedUserProfilePictures -> {
                    if (cachedUserProfilePictures.hasFeaturedInCache()) {
                        return responseTransformer.toResponse(cachedUserProfilePictures.featured());
                    }
                    throw new UserProfileFeaturedNotInCacheException(command.userPseudo());
                })
                .onFailure()
                .recoverWithUni(() ->
                        Uni.createFrom()
                                .deferred(() -> userProfilePictureRepository.getLast(command.userPseudo(), command.mediaType()))
                                .chain(profilePictureIdentifier -> userProfilePictureCacheRepository.storeFeatured(command.userPseudo(), profilePictureIdentifier)
                                        .onItemOrFailure().transform((item, exception) -> profilePictureIdentifier))
                                .map(profilePictureIdentifier -> responseTransformer.toResponse(profilePictureIdentifier))
                                .onFailure(UserProfilePictureNotAvailableYetException.class)
                                .recoverWithItem(profilePictureNotAvailableYetException -> responseTransformer.toResponse((UserProfilePictureNotAvailableYetException) profilePictureNotAvailableYetException))
                                .onFailure(UserProfilePictureRepositoryException.class)
                                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((UserProfilePictureRepositoryException) profilePictureRepositoryException))
                                .onFailure()
                                .recoverWithItem(exception -> responseTransformer.toResponse(exception))
                )
                .onTermination()
                .invoke(() -> lockMechanism.unlock(command.userPseudo()));
    }

}
