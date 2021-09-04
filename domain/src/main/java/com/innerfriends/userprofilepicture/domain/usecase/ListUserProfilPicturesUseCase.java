package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.ProfilePictureRepository;
import com.innerfriends.userprofilepicture.domain.ProfilePictureRepositoryException;
import com.innerfriends.userprofilepicture.domain.ResponseTransformer;
import com.innerfriends.userprofilepicture.domain.UseCase;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class ListUserProfilPicturesUseCase<R> implements UseCase<R, ListUserProfilPicturesCommand> {

    private final ProfilePictureRepository profilePictureRepository;

    public ListUserProfilPicturesUseCase(final ProfilePictureRepository profilePictureRepository) {
        this.profilePictureRepository = Objects.requireNonNull(profilePictureRepository);
    }

    @Override
    public Uni<R> execute(final ListUserProfilPicturesCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> profilePictureRepository.listByUserPseudo(command.userPseudo(), command.mediaType()))
                .onItem()
                .transform(profilePictureIdentifiers -> responseTransformer.toResponse(profilePictureIdentifiers))
                .onFailure(ProfilePictureRepositoryException.class)
                .recoverWithItem(profilePictureRepositoryException -> responseTransformer.toResponse((ProfilePictureRepositoryException) profilePictureRepositoryException))
                .onFailure()
                .recoverWithItem(exception -> responseTransformer.toResponse(exception));
    }

}
