package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class GetFeaturedUserProfilePictureUseCase<R> implements UseCase<R, GetFeaturedUserProfilePictureCommand> {

    private final ProfilePictureRepository profilePictureRepository;

    public GetFeaturedUserProfilePictureUseCase(final ProfilePictureRepository profilePictureRepository) {
        this.profilePictureRepository = Objects.requireNonNull(profilePictureRepository);
    }

    @Override
    public Uni<R> execute(final GetFeaturedUserProfilePictureCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> profilePictureRepository.getLast(command.userPseudo(), command.mediaType()))
                .onItem()
                .transform(profilePicture -> responseTransformer.toResponse(profilePicture))
                .onFailure(ProfilePictureNotAvailableYetException.class)
                .recoverWithItem(profilePictureNotAvailableYetException -> responseTransformer.toResponse((ProfilePictureNotAvailableYetException) profilePictureNotAvailableYetException))
                .onFailure(ProfilePictureRepositoryException.class)
                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((ProfilePictureRepositoryException) profilePictureRepositoryException))
                .onFailure()
                .recoverWithItem(exception -> responseTransformer.toResponse(exception));
    }

}
