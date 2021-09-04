package com.innerfriends.userprofilepicture.infrastructure;

import com.innerfriends.userprofilepicture.domain.ProfilePictureRepository;
import com.innerfriends.userprofilepicture.domain.usecase.GetFeaturedUserProfilePictureUseCase;
import com.innerfriends.userprofilepicture.domain.usecase.GetUserProfilePictureByVersionUseCase;
import com.innerfriends.userprofilepicture.domain.usecase.ListUserProfilPicturesUseCase;
import com.innerfriends.userprofilepicture.domain.usecase.SaveUserProfilePictureUseCase;
import com.innerfriends.userprofilepicture.infrastructure.s3.S3ObjectKey;
import com.innerfriends.userprofilepicture.infrastructure.s3.S3ObjectKeyProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class Application {

    @ApplicationScoped
    @Produces
    public GetFeaturedUserProfilePictureUseCase<Response> getFeaturedUserProfilePictureUseCaseProducer(final ProfilePictureRepository profilePictureRepository) {
        return new GetFeaturedUserProfilePictureUseCase<>(profilePictureRepository);
    }

    @ApplicationScoped
    @Produces
    public SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCaseProducer(final ProfilePictureRepository profilePictureRepository) {
        return new SaveUserProfilePictureUseCase<>(profilePictureRepository);
    }

    @ApplicationScoped
    @Produces
    public ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCaseProducer(final ProfilePictureRepository profilePictureRepository) {
        return new ListUserProfilPicturesUseCase<>(profilePictureRepository);
    }

    @ApplicationScoped
    @Produces
    public GetUserProfilePictureByVersionUseCase<Response> getUserProfilePictureByVersionUseCaseProducer(final ProfilePictureRepository profilePictureRepository) {
        return new GetUserProfilePictureByVersionUseCase<>(profilePictureRepository);
    }

    @ApplicationScoped
    @Produces
    public S3ObjectKeyProvider s3ObjectKeyProvider() {
        return (userPseudo, mediaType) -> new S3ObjectKey(userPseudo, mediaType);
    }

}
