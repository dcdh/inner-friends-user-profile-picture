package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class GetUserProfilePictureByVersionUseCase<R> implements UseCase<R, GetUserUserProfilePictureByVersionCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;

    public GetUserProfilePictureByVersionUseCase(final UserProfilePictureRepository userProfilePictureRepository) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
    }

    @Override
    public Uni<R> execute(final GetUserUserProfilePictureByVersionCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> userProfilePictureRepository.getContentByVersionId(command))
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
