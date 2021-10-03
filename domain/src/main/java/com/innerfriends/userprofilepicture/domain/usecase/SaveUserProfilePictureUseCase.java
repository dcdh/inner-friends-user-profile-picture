package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class SaveUserProfilePictureUseCase<R> implements UseCase<R, SaveUserProfilePictureCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private final LockMechanism lockMechanism;

    public SaveUserProfilePictureUseCase(final UserProfilePictureRepository userProfilePictureRepository,
                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                         final LockMechanism lockMechanism) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
        this.lockMechanism = Objects.requireNonNull(lockMechanism);
    }

    @Override
    public Uni<R> execute(final SaveUserProfilePictureCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> lockMechanism.lock(command.userPseudo()))
                .chain(() -> userProfilePictureRepository.save(command.userPseudo(), command.picture(), command.mediaType()))
                .chain(profilePictureSaved -> userProfilePictureCacheRepository.evict(command.userPseudo())
                        .onItemOrFailure().transform((item, exception) -> profilePictureSaved))
                .map(profilePictureSaved -> responseTransformer.toResponse(profilePictureSaved))
                .onFailure(UserProfilePictureRepositoryException.class)
                .recoverWithItem(userProfilePictureRepositoryException -> responseTransformer.toResponse((UserProfilePictureRepositoryException) userProfilePictureRepositoryException))
                .onFailure()
                .recoverWithItem(exception -> responseTransformer.toResponse(exception))
                .onTermination()
                .invoke(() -> lockMechanism.unlock(command.userPseudo()));
    }

}
