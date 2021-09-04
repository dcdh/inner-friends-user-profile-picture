package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@QuarkusTest
public class S3ProfilePictureRepositoryTest {

    @Inject
    S3ProfilePictureRepository s3ProfilePictureRepository;

    @ConfigProperty(name = "bucket.user.profile.picture.name")
    String bucketUserProfilePictureName;

    @Inject
    S3Client s3Client;

    @InjectMock
    S3ObjectKeyProvider s3ObjectKeyProvider;

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

        // When
        final Uni<ProfilePictureSaved> uni = s3ProfilePictureRepository.save(
                userPseudo,
                "picture".getBytes(),
                SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<ProfilePictureSaved> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final ProfilePictureSaved profilePictureSaved = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(profilePictureSaved.userPseudo().pseudo()).isEqualTo("user");
        assertThat(profilePictureSaved.versionId()).isNotNull();
        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
    }

    @Test
    public void should_save_multiple_versions_of_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        s3ProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();

        // When
        s3ProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();

        // Then
        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("user")
                .build()).versions();
        assertThat(objectVersions.size()).isEqualTo(2);
        verify(s3ObjectKeyProvider, times(2)).objectKey(any(), any());
    }

    @Test
    public void should_save_return_profile_picture_repository_exception_when_object_key_value_is_invalid() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        doReturn((ObjectKey) () -> null)
                .when(s3ObjectKeyProvider).objectKey(userPseudo, supportedMediaType);

        // When
        final Uni<ProfilePictureSaved> uni = s3ProfilePictureRepository.save(
                userPseudo,
                "picture".getBytes(),
                supportedMediaType);

        // Then
        final UniAssertSubscriber<ProfilePictureSaved> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(ProfilePictureRepositoryException.class);
        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
    }

    @Test
    public void should_get_last_version_of_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        s3ProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();
        s3ProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();

        // When
        final Uni<ContentProfilePicture> uni = s3ProfilePictureRepository.getLast(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<ContentProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final ContentProfilePicture contentProfilePicture = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(contentProfilePicture.userPseudo().pseudo()).isEqualTo("user");
        assertThat(contentProfilePicture.picture()).isEqualTo("picture".getBytes());
        assertThat(contentProfilePicture.mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(contentProfilePicture.contentLength()).isEqualTo(7l);
        assertThat(contentProfilePicture.versionId()).isNotNull();

        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("user")
                .build()).versions();
        final ObjectVersion lastObjectVersion = objectVersions.get(0);
        assertThat(lastObjectVersion.versionId()).isEqualTo(contentProfilePicture.versionId().version());
        assertThat(lastObjectVersion.isLatest()).isEqualTo(true);
        verify(s3ObjectKeyProvider, times(3)).objectKey(any(), any());
    }

    @Test
    public void should_get_last_return_profile_picture_not_available_yet_exception_when_no_picture_has_been_saved() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // When
        final Uni<ContentProfilePicture> uni = s3ProfilePictureRepository.getLast(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<ContentProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(ProfilePictureNotAvailableYetException.class);
        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
    }

    @Test
    public void should_get_last_return_profile_picture_repository_exception_when_object_key_value_is_invalid() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        doReturn((ObjectKey) () -> null)
                .when(s3ObjectKeyProvider).objectKey(userPseudo, supportedMediaType);

        // When
        final Uni<ContentProfilePicture> uni = s3ProfilePictureRepository.getLast(userPseudo, supportedMediaType);

        // Then
        final UniAssertSubscriber<ContentProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(ProfilePictureRepositoryException.class);
        verify(s3ObjectKeyProvider, times(1)).objectKey(any(), any());
    }

    @Test
    public void should_list_by_user_pseudo() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        final ProfilePictureSaved givenProfilePictureSaved = s3ProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted()
                .getItem();

        // When
        final Uni<List<ProfilePictureIdentifier>> uni = s3ProfilePictureRepository.listByUserPseudo(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<List<ProfilePictureIdentifier>> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final List<ProfilePictureIdentifier> profilePictureIdentifiers = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(profilePictureIdentifiers.size()).isEqualTo(1);
        assertThat(profilePictureIdentifiers.get(0).userPseudo().pseudo()).isEqualTo("user");
        assertThat(profilePictureIdentifiers.get(0).mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(profilePictureIdentifiers.get(0).versionId()).isEqualTo(givenProfilePictureSaved.versionId());
        verify(s3ObjectKeyProvider, times(2)).objectKey(any(), any());
    }

    @Test
    public void should_get_content_by_version_id() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);
        final ProfilePictureSaved givenProfilePictureSaved = s3ProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted()
                .getItem();

        // When
        final Uni<ContentProfilePicture> uni = s3ProfilePictureRepository.getContentByVersionId(new TestProfilePictureIdentifier(givenProfilePictureSaved));

        // Then
        final UniAssertSubscriber<ContentProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final ContentProfilePicture contentProfilePicture = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(contentProfilePicture.userPseudo().pseudo()).isEqualTo("user");
        assertThat(contentProfilePicture.picture()).isEqualTo("picture".getBytes());
        assertThat(contentProfilePicture.mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(contentProfilePicture.contentLength()).isEqualTo(7l);
        assertThat(contentProfilePicture.versionId()).isEqualTo(givenProfilePictureSaved.versionId());
        verify(s3ObjectKeyProvider, times(2)).objectKey(any(), any());
    }

    @Test
    public void should_get_content_by_version_id_return_profile_picture_version_unknown_exception_when_picture_not_found() {
        // Given
        final ProfilePictureIdentifier profilePictureIdentifier = mock(ProfilePictureIdentifier.class);
        final UserPseudo userPseudo = mock(UserPseudo.class);
        doReturn(new S3VersionId("v0")).when(profilePictureIdentifier).versionId();
        doReturn(userPseudo).when(profilePictureIdentifier).userPseudo();
        doReturn(SupportedMediaType.IMAGE_JPEG).when(profilePictureIdentifier).mediaType();
        doReturn(new S3ObjectKey(userPseudo, SupportedMediaType.IMAGE_JPEG))
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // When
        final Uni<ContentProfilePicture> uni = s3ProfilePictureRepository.getContentByVersionId(profilePictureIdentifier);

        // Then
        final UniAssertSubscriber<ContentProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(ProfilePictureVersionUnknownException.class);
        verify(profilePictureIdentifier, times(1)).versionId();
        verify(profilePictureIdentifier, times(1)).mediaType();
        verify(profilePictureIdentifier, times(1)).userPseudo();
    }

    @Test
    public void should_get_content_by_version_id_return_profile_picture_repository_exception_when_s3_object_key_is_invalid() {
        // Given
        final ProfilePictureIdentifier profilePictureIdentifier = mock(ProfilePictureIdentifier.class);
        final UserPseudo userPseudo = mock(UserPseudo.class);
        doReturn(new S3VersionId("v0")).when(profilePictureIdentifier).versionId();
        doReturn(userPseudo).when(profilePictureIdentifier).userPseudo();
        doReturn(SupportedMediaType.IMAGE_JPEG).when(profilePictureIdentifier).mediaType();
        doReturn((ObjectKey) () -> null)
                .when(s3ObjectKeyProvider).objectKey(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // When
        final Uni<ContentProfilePicture> uni = s3ProfilePictureRepository.getContentByVersionId(profilePictureIdentifier);

        // Then
        final UniAssertSubscriber<ContentProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(ProfilePictureRepositoryException.class);
        verify(profilePictureIdentifier, times(1)).versionId();
        verify(profilePictureIdentifier, times(1)).mediaType();
        verify(profilePictureIdentifier, times(1)).userPseudo();
    }

}
