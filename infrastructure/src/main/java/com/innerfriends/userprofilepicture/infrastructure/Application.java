package com.innerfriends.userprofilepicture.infrastructure;

import com.innerfriends.userprofilepicture.domain.UserProfilePictureRepository;
import com.innerfriends.userprofilepicture.domain.UserProfilePictureCacheRepository;
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
    public GetFeaturedUserProfilePictureUseCase<Response> getFeaturedUserProfilePictureUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository,
                                                                                                       final UserProfilePictureCacheRepository userProfilePictureCacheRepository) {
        return new GetFeaturedUserProfilePictureUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository);
    }

    @ApplicationScoped
    @Produces
    public SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository,
                                                                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository) {
        return new SaveUserProfilePictureUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository);
    }

    @ApplicationScoped
    @Produces
    public ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository,
                                                                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository) {
        return new ListUserProfilPicturesUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository);
    }

    @ApplicationScoped
    @Produces
    public GetUserProfilePictureByVersionUseCase<Response> getUserProfilePictureByVersionUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository) {
        return new GetUserProfilePictureByVersionUseCase<>(userProfilePictureRepository);
    }

    @ApplicationScoped
    @Produces
    public S3ObjectKeyProvider s3ObjectKeyProvider() {
        return (userPseudo, mediaType) -> new S3ObjectKey(userPseudo, mediaType);
    }

}
