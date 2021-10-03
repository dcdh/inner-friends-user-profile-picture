package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class GetFeaturedUserProfilePictureUseCase<R> implements UseCase<R, GetFeaturedUserProfilePictureCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private final LockMechanism lockMechanism;
    private final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository;

    public GetFeaturedUserProfilePictureUseCase(final UserProfilePictureRepository userProfilePictureRepository,
                                                final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                                final LockMechanism lockMechanism,
                                                final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
        this.lockMechanism = Objects.requireNonNull(lockMechanism);
        this.userProfilPictureFeaturedRepository = Objects.requireNonNull(userProfilPictureFeaturedRepository);
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
                                        .deferred(() -> userProfilPictureFeaturedRepository.getFeatured(command.userPseudo()))
                                        .chain(userProfilePictureIdentifier -> userProfilePictureCacheRepository.storeFeatured(command.userPseudo(), userProfilePictureIdentifier))
                                        .map(cachedUserProfilePictures -> cachedUserProfilePictures.featured())
                                        .onFailure()
                                        .recoverWithUni(() -> userProfilePictureRepository.getLast(command.userPseudo(), command.mediaType()))
                                        .map(userProfilePictureIdentifier -> responseTransformer.toResponse(userProfilePictureIdentifier))
                                        .onFailure(UserProfilePictureNotAvailableYetException.class)
                                        .recoverWithItem(profilePictureNotAvailableYetException -> responseTransformer.toResponse((UserProfilePictureNotAvailableYetException) profilePictureNotAvailableYetException))
                                        .onFailure(UserProfilePictureRepositoryException.class)
                                        .recoverWithItem(userProfilePictureRepositoryException -> responseTransformer.toResponse((UserProfilePictureRepositoryException) userProfilePictureRepositoryException))
                                        .onFailure()
                                        .recoverWithItem(exception -> responseTransformer.toResponse(exception))
                )
                .onTermination()
                .invoke(() -> lockMechanism.unlock(command.userPseudo()));
    }

}
