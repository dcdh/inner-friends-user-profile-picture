package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
public class S3ProfilePictureRepository implements ProfilePictureRepository {

    private final S3AsyncClient s3AsyncClient;
    private final String bucketUserProfilePictureName;

    public S3ProfilePictureRepository(final S3AsyncClient s3AsyncClient,
                                      @ConfigProperty(name = "bucket.user.profile.picture.name") final String bucketUserProfilePictureName) {
        this.s3AsyncClient = Objects.requireNonNull(s3AsyncClient);
        this.bucketUserProfilePictureName = Objects.requireNonNull(bucketUserProfilePictureName);
    }

    @Override
    public Uni<ProfilePictureSaved> save(final UserPseudo userPseudo,
                                         final byte[] picture,
                                         final SupportedMediaType mediaType) throws ProfilePictureRepositoryException {
        return Uni.createFrom()
                .completionStage(() -> {
                    final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketUserProfilePictureName)
                            .key(userPseudo.pseudo())
                            .contentType(mediaType.mimeType())
                            .build();
                    return this.s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(picture))
                            .handle((putObjectResponse, completionException) -> {
                                if (completionException != null) {
                                    throw new ProfilePictureRepositoryException();
                                } else {
                                    return new S3ProfilePictureSaved(userPseudo, putObjectResponse);
                                }
                            });
                });
    }

    @Override
    public Uni<ProfilePicture> getLast(final UserPseudo userPseudo) throws ProfilePictureNotAvailableYetException, ProfilePictureRepositoryException {
        return Uni.createFrom()
                .completionStage(() -> {
                    final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketUserProfilePictureName)
                            .key(userPseudo.pseudo()).build();
                    return s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBytes())
                            .handle((getObjectResponse, completionException) -> {
                                if (completionException != null) {
                                    final Throwable cause = completionException.getCause();
                                    if (cause instanceof NoSuchKeyException) {
                                        throw new ProfilePictureNotAvailableYetException(userPseudo);
                                    } else {
                                        completionException.printStackTrace();
                                        throw new ProfilePictureRepositoryException();
                                    }
                                } else {
                                    return new S3ProfilePicture(userPseudo, getObjectResponse);
                                }
                            });
                });
    }

}
