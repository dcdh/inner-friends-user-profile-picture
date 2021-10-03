package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class MarkUserProfilePictureAsFeaturedUseCase<R> implements UseCase<R, MarkUserProfilePictureAsFeaturedCommand> {

    private final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private final LockMechanism lockMechanism;

    public MarkUserProfilePictureAsFeaturedUseCase(final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository,
                                                   final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                                   final LockMechanism lockMechanism) {
        this.userProfilPictureFeaturedRepository = Objects.requireNonNull(userProfilPictureFeaturedRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
        this.lockMechanism = Objects.requireNonNull(lockMechanism);
    }

    @Override
    public Uni<R> execute(final MarkUserProfilePictureAsFeaturedCommand command, final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> lockMechanism.lock(command.userPseudo()))
                .chain(() -> userProfilPictureFeaturedRepository.markAsFeatured(command))
                .chain(userProfilePictureIdentifier -> userProfilePictureCacheRepository.evict(command.userPseudo())
                        .onItemOrFailure().transform((item, exception) -> userProfilePictureIdentifier))
                .map(userProfilePictureIdentifier -> responseTransformer.toResponse(userProfilePictureIdentifier))
                .onFailure(UserProfilPictureFeaturedRepositoryException.class)
                .recoverWithItem(userProfilPictureFeaturedRepositoryException -> responseTransformer.toResponse((UserProfilPictureFeaturedRepositoryException) userProfilPictureFeaturedRepositoryException))
                .onFailure(RuntimeException.class)
                .recoverWithItem(userProfilPictureFeaturedRepositoryException -> responseTransformer.toResponse(userProfilPictureFeaturedRepositoryException))
                .onTermination()
                .invoke(() -> lockMechanism.unlock(command.userPseudo()));
    }

}
