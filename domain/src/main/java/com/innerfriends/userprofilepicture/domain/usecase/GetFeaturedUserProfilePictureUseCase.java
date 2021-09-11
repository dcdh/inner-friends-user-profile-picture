package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class GetFeaturedUserProfilePictureUseCase<R> implements UseCase<R, GetFeaturedUserProfilePictureCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;

    public GetFeaturedUserProfilePictureUseCase(final UserProfilePictureRepository userProfilePictureRepository,
                                                final UserProfilePictureCacheRepository userProfilePictureCacheRepository) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
    }

    @Override
    public Uni<R> execute(final GetFeaturedUserProfilePictureCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> userProfilePictureCacheRepository.get(command.userPseudo()))
                .map(cachedUserProfilePictures -> responseTransformer.toResponse(cachedUserProfilePictures.featured()))
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
                );
    }

}
