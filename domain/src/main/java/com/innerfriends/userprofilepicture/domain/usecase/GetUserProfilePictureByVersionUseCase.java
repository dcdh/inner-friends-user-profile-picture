package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class GetUserProfilePictureByVersionUseCase<R> implements UseCase<R, GetUserProfilePictureByVersionCommand> {

    private final ProfilePictureRepository profilePictureRepository;

    public GetUserProfilePictureByVersionUseCase(final ProfilePictureRepository profilePictureRepository) {
        this.profilePictureRepository = Objects.requireNonNull(profilePictureRepository);
    }

    @Override
    public Uni<R> execute(final GetUserProfilePictureByVersionCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> profilePictureRepository.getContentByVersionId(command))
                .onItem()
                .transform(contentProfilePicture -> responseTransformer.toResponse(contentProfilePicture))
                .onFailure(ProfilePictureVersionUnknownException.class)
                .recoverWithItem(profilePictureVersionUnknownException -> responseTransformer.toResponse((ProfilePictureVersionUnknownException) profilePictureVersionUnknownException))
                .onFailure(ProfilePictureRepositoryException.class)
                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((ProfilePictureRepositoryException) profilePictureRepositoryException))
                .onFailure()
                .recoverWithItem(exception -> responseTransformer.toResponse(exception));
    }
}
