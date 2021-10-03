package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;

import java.util.Objects;

public class ListUserProfilPicturesUseCase<R> implements UseCase<R, ListUserProfilPicturesCommand> {

    private final UserProfilePictureRepository userProfilePictureRepository;
    private final UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private final LockMechanism lockMechanism;
    private final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository;

    public ListUserProfilPicturesUseCase(final UserProfilePictureRepository userProfilePictureRepository,
                                         final UserProfilePictureCacheRepository userProfilePictureCacheRepository,
                                         final LockMechanism lockMechanism,
                                         final UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository) {
        this.userProfilePictureRepository = Objects.requireNonNull(userProfilePictureRepository);
        this.userProfilePictureCacheRepository = Objects.requireNonNull(userProfilePictureCacheRepository);
        this.lockMechanism = Objects.requireNonNull(lockMechanism);
        this.userProfilPictureFeaturedRepository = Objects.requireNonNull(userProfilPictureFeaturedRepository);
    }

    @Override
    public Uni<R> execute(final ListUserProfilPicturesCommand command,
                          final ResponseTransformer<R> responseTransformer) {
        return Uni.createFrom()
                .deferred(() -> lockMechanism.lock(command.userPseudo()))
                .chain(() -> userProfilePictureCacheRepository.get(command.userPseudo()))
                .map(cachedUserProfilePictures -> {
                    if (cachedUserProfilePictures.hasUserProfilePictureIdentifiersInCache()) {
                        return responseTransformer.toResponse(cachedUserProfilePictures);
                    }
                    throw new UserProfilePicturesNotInCacheException(command.userPseudo());
                })
                .onFailure()
                .recoverWithUni(() ->
                        Uni.createFrom()
                                .deferred(() -> userProfilePictureRepository.listByUserPseudo(command.userPseudo(), command.mediaType()))
                                .chain(userProfilePictureIdentifiers -> userProfilPictureFeaturedRepository.getFeatured(command.userPseudo())
                                        .map(featuredUserProfilePictureIdentifier -> DomainUserProfilePictures.newBuilder()
                                                .withFeaturedStateSelected(userProfilePictureIdentifiers, featuredUserProfilePictureIdentifier).build())
                                        .onFailure(NoUserProfilPictureFeaturedYetException.class)
                                        .recoverWithItem(() -> DomainUserProfilePictures.newBuilder()
                                                .withFeaturedStateNotSelectedYet(userProfilePictureIdentifiers).build())
                                        .onFailure(UserProfilPictureFeaturedRepositoryException.class)
                                        .recoverWithItem(() -> DomainUserProfilePictures.newBuilder()
                                                .withFeaturedStateInErrorWhenRetrieving(userProfilePictureIdentifiers).build())
                                        .onFailure()
                                        .recoverWithItem(() -> DomainUserProfilePictures.newBuilder()
                                                .withFeaturedStateInErrorWhenRetrieving(userProfilePictureIdentifiers).build())
                                )
                                .chain(userProfilePictures -> userProfilePictureCacheRepository.store(command.userPseudo(), userProfilePictures)
                                        .onItemOrFailure().transform((item, exception) -> userProfilePictures))
                                .map(domainUserProfilePictures -> responseTransformer.toResponse(domainUserProfilePictures))
                                .onFailure(UserProfilePictureRepositoryException.class)
                                .recoverWithItem(userProfilePictureRepositoryException -> responseTransformer.toResponse((UserProfilePictureRepositoryException) userProfilePictureRepositoryException))
                                .onFailure()
                                .recoverWithItem(exception -> responseTransformer.toResponse(exception))
                )
                .onTermination()
                .invoke(() -> lockMechanism.unlock(command.userPseudo()));
    }

}
