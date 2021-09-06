package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class S3ProfilePictureRepository implements ProfilePictureRepository {

    private final S3AsyncClient s3AsyncClient;
    private final String bucketUserProfilePictureName;
    private final S3ObjectKeyProvider s3ObjectKeyProvider;
    private final Tracer tracer;

    private static final Logger LOG = Logger.getLogger(S3ProfilePictureRepository.class);

    public S3ProfilePictureRepository(final S3AsyncClient s3AsyncClient,
                                      @ConfigProperty(name = "bucket.user.profile.picture.name") final String bucketUserProfilePictureName,
                                      final S3ObjectKeyProvider s3ObjectKeyProvider,
                                      final Tracer tracer) {
        this.s3AsyncClient = Objects.requireNonNull(s3AsyncClient);
        this.bucketUserProfilePictureName = Objects.requireNonNull(bucketUserProfilePictureName);
        this.s3ObjectKeyProvider = Objects.requireNonNull(s3ObjectKeyProvider);
        this.tracer = Objects.requireNonNull(tracer);
    }

    @Override
    public Uni<ProfilePictureSaved> save(final UserPseudo userPseudo,
                                         final byte[] picture,
                                         final SupportedMediaType mediaType) throws ProfilePictureRepositoryException {
        return Uni.createFrom()
                .deferred(() -> {
                    final SpanBuilder spanBuilder = tracer.spanBuilder("S3ProfilePictureRepository.save");
                    final Span span = spanBuilder.startSpan();
                    final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketUserProfilePictureName)
                            .key(s3ObjectKeyProvider.objectKey(userPseudo, mediaType).value())
                            .contentType(mediaType.contentType())
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(picture)))
                            .map(putObjectResponse -> {
                                span.end();
                                return new S3ProfilePictureSaved(userPseudo, mediaType, putObjectResponse);
                            })
                            .onFailure(SdkException.class)
                            .transform(exception -> {
                                LOG.error(exception);
                                span.setStatus(StatusCode.ERROR);
                                span.end();
                                return new ProfilePictureRepositoryException();
                            });
                });
    }

    @Override
    public Uni<ProfilePictureIdentifier> getLast(final UserPseudo userPseudo, final SupportedMediaType mediaType)
            throws ProfilePictureNotAvailableYetException, ProfilePictureRepositoryException {
        return Uni.createFrom()
                .deferred(() -> {
                    final SpanBuilder spanBuilder = tracer.spanBuilder("S3ProfilePictureRepository.getLast");
                    final Span span = spanBuilder.startSpan();
                    final ListObjectVersionsRequest listObjectVersionsRequest = ListObjectVersionsRequest.builder()
                            .bucket(bucketUserProfilePictureName)
                            .prefix(s3ObjectKeyProvider.objectKey(userPseudo, mediaType).value())
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> s3AsyncClient.listObjectVersions(listObjectVersionsRequest))
                            .map(listObjectVersionsResponse -> {
                                span.end();
                                final Optional<ProfilePictureIdentifier> profilePictureIdentifier = listObjectVersionsResponse.versions()
                                        .stream()
                                        .findFirst()
                                        .map(objectVersion -> new S3ProfilePictureIdentifier(userPseudo, mediaType, objectVersion));
                                if (!profilePictureIdentifier.isPresent()) {
                                    throw new ProfilePictureNotAvailableYetException(userPseudo);
                                }
                                return profilePictureIdentifier.get();
                            })
                            .onFailure(SdkException.class)
                            .transform(exception -> {
                                LOG.error(exception);
                                span.setStatus(StatusCode.ERROR);
                                span.end();
                                return new ProfilePictureRepositoryException();
                            });
                });
    }

    @Override
    public Uni<List<? extends ProfilePictureIdentifier>> listByUserPseudo(final UserPseudo userPseudo,
                                                                final SupportedMediaType mediaType)
            throws ProfilePictureRepositoryException {
        return Uni.createFrom()
                .deferred(() -> {
                    final SpanBuilder spanBuilder = tracer.spanBuilder("S3ProfilePictureRepository.listByUserPseudo");
                    final Span span = spanBuilder.startSpan();
                    final ListObjectVersionsRequest listObjectVersionsRequest = ListObjectVersionsRequest.builder()
                            .bucket(bucketUserProfilePictureName)
                            .prefix(s3ObjectKeyProvider.objectKey(userPseudo, mediaType).value())
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> s3AsyncClient.listObjectVersions(listObjectVersionsRequest))
                            .map(listObjectVersionsResponse -> {
                                span.end();
                                return listObjectVersionsResponse
                                        .versions()
                                        .stream()
                                        .map(objectVersion -> new S3ProfilePictureIdentifier(userPseudo, mediaType, objectVersion))
                                        .collect(Collectors.toList());
                            })
                            .onFailure(SdkException.class)
                            .transform(exception -> {
                                LOG.error(exception);
                                span.setStatus(StatusCode.ERROR);
                                throw new ProfilePictureRepositoryException();
                            });
                });
    }

    @Override
    public Uni<ContentProfilePicture> getContentByVersionId(final ProfilePictureIdentifier profilePictureIdentifier)
            throws ProfilePictureVersionUnknownException, ProfilePictureRepositoryException {
        return Uni.createFrom()
                .deferred(() -> {
                    final SpanBuilder spanBuilder = tracer.spanBuilder("S3ProfilePictureRepository.getContentByVersion");
                    final Span span = spanBuilder.startSpan();
                    final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketUserProfilePictureName)
                            .key(s3ObjectKeyProvider.objectKey(profilePictureIdentifier.userPseudo(), profilePictureIdentifier.mediaType()).value())
                            .versionId(profilePictureIdentifier.versionId().version())
                            .build();
                    return Uni.createFrom()
                            .completionStage(() -> s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()))
                            .map(getObjectResponse -> {
                                span.end();
                                return new S3ContentProfilePicture(profilePictureIdentifier.userPseudo(), getObjectResponse);
                            })
                            .onFailure(NoSuchKeyException.class)
                            .transform(exception -> {
                                span.setStatus(StatusCode.ERROR);
                                span.end();
                                return new ProfilePictureVersionUnknownException(profilePictureIdentifier);
                            })
                            .onFailure(SdkException.class)
                            .transform(exception -> {
                                span.setStatus(StatusCode.ERROR);
                                span.end();
                                if (exception.getMessage().startsWith("Invalid version id specified")) {
                                    return new ProfilePictureVersionUnknownException(profilePictureIdentifier);
                                }
                                return new ProfilePictureRepositoryException();
                            });
                });
    }

}
