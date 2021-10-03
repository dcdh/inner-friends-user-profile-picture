package com.innerfriends.userprofilepicture.infrastructure;

import com.innerfriends.userprofilepicture.domain.LockMechanism;
import com.innerfriends.userprofilepicture.domain.UserProfilPictureFeaturedRepository;
import com.innerfriends.userprofilepicture.domain.UserProfilePictureRepository;
import com.innerfriends.userprofilepicture.domain.UserProfilePictureCacheRepository;
import com.innerfriends.userprofilepicture.domain.usecase.*;
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
                                                                                                       final LockMechanism lockMechanism,
                                                                                                       final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository) {
        return new GetFeaturedUserProfilePictureUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism, userProfilPictureFeaturedRepository);
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
                                                                                         final LockMechanism lockMechanism,
                                                                                         final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository) {
        return new ListUserProfilPicturesUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism, userProfilPictureFeaturedRepository);
    }

    @ApplicationScoped
    @Produces
    public GetUserProfilePictureByVersionUseCase<Response> getUserProfilePictureByVersionUseCaseProducer(final UserProfilePictureRepository userProfilePictureRepository,
                                                                                                         final LockMechanism lockMechanism) {
        return new GetUserProfilePictureByVersionUseCase<>(userProfilePictureRepository, lockMechanism);
    }

    @ApplicationScoped
    @Produces
    public MarkUserProfilePictureAsFeaturedUseCase<Response> markUserProfilePictureAsFeaturedUseCaseProducer(final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository,
                                                                                                             final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                                                                                             final LockMechanism lockMechanism) {
        return new MarkUserProfilePictureAsFeaturedUseCase<>(userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @ApplicationScoped
    @Produces
    public S3ObjectKeyProvider s3ObjectKeyProvider() {
        return (userPseudo, mediaType) -> new S3ObjectKey(userPseudo, mediaType);
    }

}
