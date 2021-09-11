package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;
import com.innerfriends.userprofilepicture.infrastructure.tracing.OpenTelemetryTracingService;
import io.opentelemetry.api.trace.Span;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@QuarkusTest
public class S3UserProfilePictureRepositoryTest {

    @Inject
    S3UserProfilePictureRepository s3UserProfilePictureRepository;

    @ConfigProperty(name = "bucket.user.profile.picture.name")
    String bucketUserProfilePictureName;

    @Inject
    S3Client s3Client;

    @InjectMock
    S3ObjectKeyProvider s3ObjectKeyProvider;

    @InjectMock
    OpenTelemetryTracingService openTelemetryTracingService;

    @InjectSpy
    S3AsyncClient s3AsyncClient;

    @BeforeEach
    @AfterEach
    public void flush() {
        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .build()).versions();
        objectVersions.stream().forEach(objectVersion -> {
            System.out.println("Delete s3 object " + objectVersion);
            s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucketUserProfilePictureName)
                    .delete(Delete.builder()
                            .objects(ObjectIdentifier.builder()
                                    .key(objectVersion.key())
                                    .versionId(objectVersion.versionId())
                                    .build()).build())
                    .build());
        });
    }

    @Test
    public void should_save_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan("S3ProfilePictureRepository.save");
        final InOrder inOrder = inOrder(openTelemetryTracingService, s3AsyncClient);

        // When
        final Uni<UserProfilePictureSaved> uni = s3UserProfilePictureRepository.save(
                userPseudo,
                "picture".getBytes(),
                SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<UserProfilePictureSaved> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final UserProfilePictureSaved profilePictureSaved = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(profilePictureSaved.userPseudo().pseudo()).isEqualTo("user");
        assertThat(profilePictureSaved.versionId()).isNotNull();

        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("user")
                .build()).versions();
        assertThat(objectVersions.size()).isEqualTo(1);
        assertThat(objectVersions.get(0).versionId()).isEqualTo(profilePictureSaved.versionId().version());
        assertThat(objectVersions.get(0).key()).isEqualTo("user.jpeg");

        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(s3AsyncClient, times(1)).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_save_multiple_versions_of_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());
        s3UserProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();

        // When
        s3UserProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();

        // Then
        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("user")
                .build()).versions();
        assertThat(objectVersions.size()).isEqualTo(2);
        verify(s3ObjectKeyProvider, times(2)).objectKey(any(), any());
        verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
    }

    @Test
    public void should_save_return_profile_picture_repository_exception_when_object_key_value_is_invalid() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        doReturn((ObjectKey) () -> null)
                .when(s3ObjectKeyProvider).objectKey(userPseudo, supportedMediaType);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());
        final InOrder inOrder = inOrder(openTelemetryTracingService, s3AsyncClient);

        // When
        final Uni<UserProfilePictureSaved> uni = s3UserProfilePictureRepository.save(
                userPseudo,
                "picture".getBytes(),
                supportedMediaType);

        // Then
        final UniAssertSubscriber<UserProfilePictureSaved> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(UserProfilePictureRepositoryException.class);

        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("user")
                .build()).versions();
        assertThat(objectVersions.size()).isEqualTo(0);

        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(s3AsyncClient, times(1)).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
        inOrder.verify(openTelemetryTracingService, times(1)).markSpanInError(span);
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_get_last_version_of_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);

        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketUserProfilePictureName)
                .key("user.jpeg")
                .contentType("image/jpeg")
                .build();
        s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes("picture".getBytes()));
        s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes("picture".getBytes()));

        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan("S3ProfilePictureRepository.getLast");
        final InOrder inOrder = inOrder(openTelemetryTracingService, s3AsyncClient);

        // When
        final Uni<UserProfilePictureIdentifier> uni = s3UserProfilePictureRepository.getLast(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<UserProfilePictureIdentifier> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final UserProfilePictureIdentifier userProfilePictureIdentifier = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(userProfilePictureIdentifier.userPseudo().pseudo()).isEqualTo("user");
        assertThat(userProfilePictureIdentifier.mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(userProfilePictureIdentifier.versionId()).isNotNull();

        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("user")
                .build()).versions();
        final ObjectVersion lastObjectVersion = objectVersions.get(0);
        assertThat(lastObjectVersion.versionId()).isEqualTo(userProfilePictureIdentifier.versionId().version());
        assertThat(lastObjectVersion.isLatest()).isEqualTo(true);
        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(s3AsyncClient, times(1)).listObjectVersions(any(ListObjectVersionsRequest.class));
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_get_last_return_profile_picture_not_available_yet_exception_when_no_picture_has_been_saved() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());
        final InOrder inOrder = inOrder(openTelemetryTracingService, s3AsyncClient);

        // When
        final Uni<UserProfilePictureIdentifier> uni = s3UserProfilePictureRepository.getLast(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<UserProfilePictureIdentifier> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(UserProfilePictureNotAvailableYetException.class);
        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(s3AsyncClient, times(1)).listObjectVersions(any(ListObjectVersionsRequest.class));
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_list_by_user_pseudo() throws Exception {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketUserProfilePictureName)
                .key("user.jpeg")
                .contentType("image/jpeg")
                .build();
        final String versionId = s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes("picture".getBytes())).get().versionId();

        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan("S3ProfilePictureRepository.listByUserPseudo");
        final InOrder inOrder = inOrder(openTelemetryTracingService, s3AsyncClient);

        // When
        final Uni<List<? extends UserProfilePictureIdentifier>> uni = s3UserProfilePictureRepository.listByUserPseudo(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<List<? extends UserProfilePictureIdentifier>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final List<? extends UserProfilePictureIdentifier> userProfilePictureIdentifiers = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(userProfilePictureIdentifiers.size()).isEqualTo(1);
        assertThat(userProfilePictureIdentifiers.get(0).userPseudo().pseudo()).isEqualTo("user");
        assertThat(userProfilePictureIdentifiers.get(0).mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(userProfilePictureIdentifiers.get(0).versionId().version()).isEqualTo(versionId);
        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(s3AsyncClient, times(1)).listObjectVersions(any(ListObjectVersionsRequest.class));
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_get_content_by_version_id() throws Exception {
        // Given
        final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketUserProfilePictureName)
                .key("user.jpeg")
                .contentType("image/jpeg")
                .build();
        final String versionId = s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes("picture".getBytes())).get().versionId();
        final TestUserProfilePictureIdentifier testProfilePictureIdentifier = new TestUserProfilePictureIdentifier(versionId);
        doReturn(new S3ObjectKey(testProfilePictureIdentifier.userPseudo(), testProfilePictureIdentifier.mediaType()))
                .when(s3ObjectKeyProvider).objectKey(testProfilePictureIdentifier.userPseudo(), testProfilePictureIdentifier.mediaType());
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan("S3ProfilePictureRepository.getContentByVersion");
        final InOrder inOrder = inOrder(openTelemetryTracingService, s3AsyncClient);

        // When
        final Uni<ContentUserProfilePicture> uni = s3UserProfilePictureRepository.getContentByVersionId(new TestUserProfilePictureIdentifier(versionId));

        // Then
        final UniAssertSubscriber<ContentUserProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final ContentUserProfilePicture contentUserProfilePicture = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(contentUserProfilePicture.userPseudo().pseudo()).isEqualTo("user");
        assertThat(contentUserProfilePicture.picture()).isEqualTo("picture".getBytes());
        assertThat(contentUserProfilePicture.mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(contentUserProfilePicture.contentLength()).isEqualTo(7l);
        assertThat(contentUserProfilePicture.versionId().version()).isEqualTo(versionId);
        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(s3AsyncClient, times(1)).getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class));
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_get_content_by_version_id_return_profile_picture_version_unknown_exception_when_picture_not_found() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        final UserPseudo userPseudo = mock(UserPseudo.class);
        doReturn(new S3VersionId("v0")).when(userProfilePictureIdentifier).versionId();
        doReturn(userPseudo).when(userProfilePictureIdentifier).userPseudo();
        doReturn(SupportedMediaType.IMAGE_JPEG).when(userProfilePictureIdentifier).mediaType();
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());
        final InOrder inOrder = inOrder(openTelemetryTracingService, s3AsyncClient);

        // When
        final Uni<ContentUserProfilePicture> uni = s3UserProfilePictureRepository.getContentByVersionId(userProfilePictureIdentifier);

        // Then
        final UniAssertSubscriber<ContentUserProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(UserProfilePictureVersionUnknownException.class);
        verify(userProfilePictureIdentifier, times(1)).versionId();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(s3AsyncClient, times(1)).getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class));
        inOrder.verify(openTelemetryTracingService, times(1)).markSpanInError(span);
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_get_content_by_version_id_return_profile_picture_repository_exception_when_s3_object_key_is_invalid() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        final UserPseudo userPseudo = mock(UserPseudo.class);
        doReturn(new S3VersionId("v0")).when(userProfilePictureIdentifier).versionId();
        doReturn(userPseudo).when(userProfilePictureIdentifier).userPseudo();
        doReturn(SupportedMediaType.IMAGE_JPEG).when(userProfilePictureIdentifier).mediaType();
        doReturn((ObjectKey) () -> null)
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());
        final InOrder inOrder = inOrder(openTelemetryTracingService, s3AsyncClient);

        // When
        final Uni<ContentUserProfilePicture> uni = s3UserProfilePictureRepository.getContentByVersionId(userProfilePictureIdentifier);

        // Then
        final UniAssertSubscriber<ContentUserProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(UserProfilePictureRepositoryException.class);
        verify(userProfilePictureIdentifier, times(1)).versionId();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(s3AsyncClient, times(1)).getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class));
        inOrder.verify(openTelemetryTracingService, times(1)).markSpanInError(span);
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

}
