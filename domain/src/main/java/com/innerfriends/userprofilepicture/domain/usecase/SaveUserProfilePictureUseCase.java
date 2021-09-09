package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class SaveUserProfilePictureUseCase<R> implements UseCase<R, SaveUserProfilePictureCommand> {

    private final ProfilePictureRepository profilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;

    public SaveUserProfilePictureUseCase(final ProfilePictureRepository profilePictureRepository,
                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository) {
        this.profilePictureRepository = Objects.requireNonNull(profilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
    }

    @Override
    public Uni<R> execute(final SaveUserProfilePictureCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> profilePictureRepository.save(command.userPseudo(), command.picture(), command.mediaType()))
                .chain(profilePictureSaved -> userProfilePictureCacheRepository.storeFeatured(command.userPseudo(), profilePictureSaved)
                        .onItemOrFailure().transform((item, exception) -> profilePictureSaved))
                .map(profilePictureSaved -> responseTransformer.toResponse(profilePictureSaved))
                .onFailure(ProfilePictureRepositoryException.class)
                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((ProfilePictureRepositoryException) profilePictureRepositoryException))
                .onFailure()
                .recoverWithItem(exception -> responseTransformer.toResponse(exception));
    }

}
