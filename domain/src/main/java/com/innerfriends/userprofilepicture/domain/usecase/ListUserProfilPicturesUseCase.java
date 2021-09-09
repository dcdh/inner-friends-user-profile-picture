package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class ListUserProfilPicturesUseCase<R> implements UseCase<R, ListUserProfilPicturesCommand> {

    private final ProfilePictureRepository profilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;

    public ListUserProfilPicturesUseCase(final ProfilePictureRepository profilePictureRepository,
                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository) {
        this.profilePictureRepository = Objects.requireNonNull(profilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
    }

    @Override
    public Uni<R> execute(final ListUserProfilPicturesCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> userProfilePictureCacheRepository.get(command.userPseudo()))
                .map(cachedUserProfilePicture -> responseTransformer.toResponse(cachedUserProfilePicture.profilePictureIdentifiers()))
                .onFailure()
                .recoverWithUni(() ->
                        Uni.createFrom()
                                .deferred(() -> profilePictureRepository.listByUserPseudo(command.userPseudo(), command.mediaType()))
                                .chain(profilePictureIdentifiers -> userProfilePictureCacheRepository.store(command.userPseudo(), profilePictureIdentifiers)
                                        .onItemOrFailure().transform((item, exception) -> profilePictureIdentifiers))
                                .map(profilePictureIdentifiers -> responseTransformer.toResponse(profilePictureIdentifiers))
                                .onFailure(ProfilePictureRepositoryException.class)
                                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((ProfilePictureRepositoryException) profilePictureRepositoryException))
                                .onFailure()
                                .recoverWithItem(exception -> responseTransformer.toResponse(exception))
                );
    }

}
