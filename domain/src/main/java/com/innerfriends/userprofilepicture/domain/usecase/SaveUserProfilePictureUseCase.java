package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.ProfilePictureRepository;
import com.innerfriends.userprofilepicture.domain.ProfilePictureRepositoryException;
import com.innerfriends.userprofilepicture.domain.ResponseTransformer;
import com.innerfriends.userprofilepicture.domain.UseCase;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class SaveUserProfilePictureUseCase<R> implements UseCase<R, SaveUserProfilePictureCommand> {

    private final ProfilePictureRepository profilePictureRepository;

    public SaveUserProfilePictureUseCase(final ProfilePictureRepository profilePictureRepository) {
        this.profilePictureRepository = Objects.requireNonNull(profilePictureRepository);
    }

    @Override
    public Uni<R> execute(final SaveUserProfilePictureCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> profilePictureRepository.save(command.userPseudo(), command.picture(), command.mediaType()))
                .onItem()
                .transform(profilePictureSaved -> responseTransformer.toResponse(profilePictureSaved))
                .onFailure(ProfilePictureRepositoryException.class)
                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((ProfilePictureRepositoryException) profilePictureRepositoryException))
                .onFailure()
                .recoverWithItem(exception -> responseTransformer.toResponse(exception));
    }

}
