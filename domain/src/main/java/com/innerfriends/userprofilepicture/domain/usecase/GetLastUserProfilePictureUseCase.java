package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class GetLastUserProfilePictureUseCase<R> implements UseCase<R, GetLastUserProfilePictureCommand, GetLastUserProfilePictureResponseTransformer<R>> {

    private final ProfilePictureRepository profilePictureRepository;

    public GetLastUserProfilePictureUseCase(final ProfilePictureRepository profilePictureRepository) {
        this.profilePictureRepository = Objects.requireNonNull(profilePictureRepository);
    }

    @Override
    public Uni<R> execute(final GetLastUserProfilePictureCommand command,
                          final GetLastUserProfilePictureResponseTransformer<R> responseTransformer) {
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
