package com.innerfriends.userprofilepicture.infrastructure;

import com.innerfriends.userprofilepicture.domain.ProfilePictureRepository;
import com.innerfriends.userprofilepicture.domain.usecase.GetLastUserProfilePictureUseCase;
import com.innerfriends.userprofilepicture.domain.usecase.SaveUserProfilePictureUseCase;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

public class Application {

    @ApplicationScoped
    @Produces
    public GetLastUserProfilePictureUseCase<Response> getLastUserProfilePictureUseCaseProducer(final ProfilePictureRepository profilePictureRepository) {
        return new GetLastUserProfilePictureUseCase<>(profilePictureRepository);
    }

    @ApplicationScoped
    @Produces
    public SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCaseProducer(final ProfilePictureRepository profilePictureRepository) {
        return new SaveUserProfilePictureUseCase<>(profilePictureRepository);
    }

}
