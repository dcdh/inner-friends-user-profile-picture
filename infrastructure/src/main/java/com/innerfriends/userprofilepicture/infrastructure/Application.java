package com.innerfriends.userprofilepicture.infrastructure;

import com.innerfriends.userprofilepicture.domain.LockMechanism;
import com.innerfriends.userprofilepicture.domain.UserProfilePictureRepository;
import com.innerfriends.userprofilepicture.domain.UserProfilePictureCacheRepository;
import com.innerfriends.userprofilepicture.domain.usecase.GetFeaturedUserProfilePictureUseCase;
import com.innerfriends.userprofilepicture.domain.usecase.GetUserProfilePictureByVersionUseCase;
import com.innerfriends.userprofilepicture.domain.usecase.ListUserProfilPicturesUseCase;
import com.innerfriends.userprofilepicture.domain.usecase.SaveUserProfilePictureUseCase;
import com.innerfriends.userprofilepicture.infrastructure.s3.S3ObjectKey;
import com.innerfriends.userprofilepicture.infrastructure.s3.S3ObjectKeyProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.ws.rs.core.Response;

public class Application {

    @ApplicationScoped
    @Produces
    public GetFeaturedUserProfilePictureUseCase<Response> getFeaturedUserProfilePictureUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository,
                                                                                                       final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                                                                                       final LockMechanism lockMechanism) {
        return new GetFeaturedUserProfilePictureUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @ApplicationScoped
    @Produces
    public SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository,
                                                                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                                                                         final LockMechanism lockMechanism) {
        return new SaveUserProfilePictureUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @ApplicationScoped
    @Produces
    public ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository,
                                                                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                                                                         final LockMechanism lockMechanism) {
        return new ListUserProfilPicturesUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @ApplicationScoped
    @Produces
    public GetUserProfilePictureByVersionUseCase<Response> getUserProfilePictureByVersionUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository,
                                                                                                         final LockMechanism lockMechanism) {
        return new GetUserProfilePictureByVersionUseCase<>(userProfilePictureRepository, lockMechanism);
    }

    @ApplicationScoped
    @Produces
    public S3ObjectKeyProvider s3ObjectKeyProvider() {
        return (userPseudo, mediaType) -> new S3ObjectKey(userPseudo, mediaType);
    }

}
