package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class SaveUserProfilePictureUseCase<R> implements UseCase<R, SaveUserProfilePictureCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;

    public SaveUserProfilePictureUseCase(final UserProfilePictureRepository userProfilePictureRepository,
                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
    }

    @Override
    public Uni<R> execute(final SaveUserProfilePictureCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> userProfilePictureRepository.save(command.userPseudo(), command.picture(), command.mediaType()))
                .chain(profilePictureSaved -> userProfilePictureCacheRepository.storeFeatured(command.userPseudo(), profilePictureSaved)
                        .onItemOrFailure().transform((item, exception) -> profilePictureSaved))
                .map(profilePictureSaved -> responseTransformer.toResponse(profilePictureSaved))
                .onFailure(UserProfilePictureRepositoryException.class)
                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((UserProfilePictureRepositoryException) profilePictureRepositoryException))
                .onFailure()
                .recoverWithItem(exception -> responseTransformer.toResponse(exception));
    }

}
