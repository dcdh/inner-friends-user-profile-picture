package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;
import com.innerfriends.userprofilepicture.infrastructure.tracing.OpenTelemetryTracingService;
import io.opentelemetry.api.trace.Span;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class S3UserProfilePictureRepository implements UserProfilePictureRepository {

    private final S3AsyncClient s3AsyncClient;
    private final String bucketUserProfilePictureName;
    private final S3ObjectKeyProvider s3ObjectKeyProvider;
    private final OpenTelemetryTracingService openTelemetryTracingService;

    private static final Logger LOG = Logger.getLogger(S3UserProfilePictureRepository.class);

    public S3UserProfilePictureRepository(final S3AsyncClient s3AsyncClient,
                                          @ConfigProperty(name = "bucket.user.profile.picture.name") final String bucketUserProfilePictureName,
                                          final S3ObjectKeyProvider s3ObjectKeyProvider,
                                          final OpenTelemetryTracingService openTelemetryTracingService) {
        this.s3AsyncClient = Objects.requireNonNull(s3AsyncClient);
        this.bucketUserProfilePictureName = Objects.requireNonNull(bucketUserProfilePictureName);
        this.s3ObjectKeyProvider = Objects.requireNonNull(s3ObjectKeyProvider);
        this.openTelemetryTracingService = Objects.requireNonNull(openTelemetryTracingService);
    }

    @Override
    public Uni<UserProfilePictureSaved> save(final UserPseudo userPseudo,
                                             final byte[] picture,
                                             final SupportedMediaType mediaType) throws UserProfilePictureRepositoryException {
        return Uni.createFrom()
                .deferred(() -> {
                    final Span span = openTelemetryTracingService.startANewSpan("S3ProfilePictureRepository.save");
                    final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketUserProfilePictureName)
                            .key(s3ObjectKeyProvider.objectKey(userPseudo, mediaType).value())
                            .contentType(mediaType.contentType())
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(picture)))
                            .map(putObjectResponse -> new S3UserProfilePictureSaved(userPseudo, mediaType, putObjectResponse))
                            .onFailure(SdkException.class)
                            .transform(exception -> {
                                LOG.error(exception);
                                openTelemetryTracingService.markSpanInError(span);
                                return new UserProfilePictureRepositoryException();
                            })
                            .onTermination()
                            .invoke(() -> openTelemetryTracingService.endSpan(span));
                });
    }

    @Override
    public Uni<UserProfilePictureIdentifier> getLast(final UserPseudo userPseudo, final SupportedMediaType mediaType)
            throws UserProfilePictureNotAvailableYetException, UserProfilePictureRepositoryException {
        return Uni.createFrom()
                .deferred(() -> {
                    final Span span = openTelemetryTracingService.startANewSpan("S3ProfilePictureRepository.getLast");
                    final ListObjectVersionsRequest listObjectVersionsRequest = ListObjectVersionsRequest.builder()
                            .bucket(bucketUserProfilePictureName)
                            .prefix(s3ObjectKeyProvider.objectKey(userPseudo, mediaType).value())
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> s3AsyncClient.listObjectVersions(listObjectVersionsRequest))
                            .map(listObjectVersionsResponse -> {
                                final Optional<UserProfilePictureIdentifier> profilePictureIdentifier = listObjectVersionsResponse.versions()
                                        .stream()
                                        .findFirst()
                                        .map(objectVersion -> new S3UserProfilePictureIdentifier(userPseudo, mediaType, objectVersion));
                                if (!profilePictureIdentifier.isPresent()) {
                                    throw new UserProfilePictureNotAvailableYetException(userPseudo);
                                }
                                return profilePictureIdentifier.get();
                            })
                            .onFailure(SdkException.class)
                            .transform(exception -> {
                                LOG.error(exception);
                                openTelemetryTracingService.markSpanInError(span);
                                return new UserProfilePictureRepositoryException();
                            })
                            .onTermination()
                            .invoke(() -> openTelemetryTracingService.endSpan(span));
                });
    }

    @Override
    public Uni<List<? extends UserProfilePictureIdentifier>> listByUserPseudo(final UserPseudo userPseudo,
                                                                              final SupportedMediaType mediaType)
            throws UserProfilePictureRepositoryException {
        return Uni.createFrom()
                .deferred(() -> {
                    final Span span = openTelemetryTracingService.startANewSpan("S3ProfilePictureRepository.listByUserPseudo");
                    final ListObjectVersionsRequest listObjectVersionsRequest = ListObjectVersionsRequest.builder()
                            .bucket(bucketUserProfilePictureName)
                            .prefix(s3ObjectKeyProvider.objectKey(userPseudo, mediaType).value())
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> s3AsyncClient.listObjectVersions(listObjectVersionsRequest))
                            .map(listObjectVersionsResponse ->
                                    listObjectVersionsResponse
                                        .versions()
                                        .stream()
                                        .map(objectVersion -> new S3UserProfilePictureIdentifier(userPseudo, mediaType, objectVersion))
                                        .collect(Collectors.toList()))
                            .onFailure(SdkException.class)
                            .transform(exception -> {
                                LOG.error(exception);
                                openTelemetryTracingService.markSpanInError(span);
                                throw new UserProfilePictureRepositoryException();
                            })
                            .onTermination()
                            .invoke(() -> openTelemetryTracingService.endSpan(span));
                });
    }

    @Override
    public Uni<ContentUserProfilePicture> getContentByVersionId(final UserProfilePictureIdentifier userProfilePictureIdentifier)
            throws UserProfilePictureVersionUnknownException, UserProfilePictureRepositoryException {
        return Uni.createFrom()
                .deferred(() -> {
                    final Span span = openTelemetryTracingService.startANewSpan("S3ProfilePictureRepository.getContentByVersion");
                    final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketUserProfilePictureName)
                            .key(s3ObjectKeyProvider.objectKey(userProfilePictureIdentifier.userPseudo(), userProfilePictureIdentifier.mediaType()).value())
                            .versionId(userProfilePictureIdentifier.versionId().version())
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()))
                            .map(getObjectResponse -> new S3ContentUserProfilePicture(userProfilePictureIdentifier.userPseudo(), getObjectResponse))
                            .onFailure(NoSuchKeyException.class)
                            .transform(exception -> {
                                openTelemetryTracingService.markSpanInError(span);
                                return new UserProfilePictureVersionUnknownException(userProfilePictureIdentifier);
                            })
                            .onFailure(SdkException.class)
                            .transform(exception -> {
                                openTelemetryTracingService.markSpanInError(span);
                                if (exception.getMessage().startsWith("Invalid version id specified")) {
                                    return new UserProfilePictureVersionUnknownException(userProfilePictureIdentifier);
                                }
                                return new UserProfilePictureRepositoryException();
                            })
                            .onTermination()
                            .invoke(() -> openTelemetryTracingService.endSpan(span));
                });
    }

}
