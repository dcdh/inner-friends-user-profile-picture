package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
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
    private final Tracer tracer;

    public S3ProfilePictureRepository(final S3AsyncClient s3AsyncClient,
                                      @ConfigProperty(name = "bucket.user.profile.picture.name") final String bucketUserProfilePictureName,
                                      final Tracer tracer) {
        this.s3AsyncClient = Objects.requireNonNull(s3AsyncClient);
        this.bucketUserProfilePictureName = Objects.requireNonNull(bucketUserProfilePictureName);
        this.tracer = Objects.requireNonNull(tracer);
    }

    @Override
    public Uni<ProfilePictureSaved> save(final UserPseudo userPseudo,
                                         final byte[] picture,
                                         final SupportedMediaType mediaType) throws ProfilePictureRepositoryException {
        return Uni.createFrom()
                .completionStage(() -> {
                    final Span parentSpan = Objects.requireNonNull(Span.current());
                    final SpanBuilder spanBuilder = tracer.spanBuilder("S3ProfilePictureRepository.save");
                    spanBuilder.setParent(Context.current().with(parentSpan));
                    final Span span = spanBuilder.startSpan();
                    final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketUserProfilePictureName)
                            .key(userPseudo.pseudo())
                            .contentType(mediaType.mimeType())
                            .build();
                    return this.s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(picture))
                            .handle((putObjectResponse, completionException) -> {
                                try {
                                    if (completionException != null) {
                                        span.setStatus(StatusCode.ERROR);
                                        throw new ProfilePictureRepositoryException();
                                    } else {
                                        return new S3ProfilePictureSaved(userPseudo, putObjectResponse);
                                    }
                                } finally {
                                    span.end();
                                }
                            });
                });
    }

    @Override
    public Uni<ProfilePicture> getLast(final UserPseudo userPseudo) throws ProfilePictureNotAvailableYetException, ProfilePictureRepositoryException {
        return Uni.createFrom()
                .completionStage(() -> {
                    final Span parentSpan = Objects.requireNonNull(Span.current());
                    final SpanBuilder spanBuilder = tracer.spanBuilder("S3ProfilePictureRepository.getLast");
                    spanBuilder.setParent(Context.current().with(parentSpan));
                    final Span span = spanBuilder.startSpan();
                    final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketUserProfilePictureName)
                            .key(userPseudo.pseudo()).build();
                    return s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBytes())
                            .handle((getObjectResponse, completionException) -> {
                                try {
                                    if (completionException != null) {
                                        span.setStatus(StatusCode.ERROR);
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
                                } finally {
                                    span.end();
                                }
                            });
                });
    }

}
