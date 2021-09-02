package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;
import io.quarkus.test.junit.QuarkusTest;
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
    }

    @Test
    public void should_save_multiple_versions_of_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = () -> "user";
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
    }

    @Test
    public void should_save_return_profile_picture_repository_exception_when_user_pseudo_name_is_invalid() {
        // Given
        final UserPseudo userPseudo = () -> "";
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        doReturn("").when(supportedMediaType).extension();

        // When
        final Uni<ProfilePictureSaved> uni = s3ProfilePictureRepository.save(
                userPseudo,
                "picture".getBytes(),
                supportedMediaType);

        // Then
        final UniAssertSubscriber<ProfilePictureSaved> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(ProfilePictureRepositoryException.class);
        verify(supportedMediaType, times(1)).extension();
    }

    @Test
    public void should_get_last_version_of_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = () -> "user";
        s3ProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();
        s3ProfilePictureRepository.save(userPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG)
                .subscribe().withSubscriber(UniAssertSubscriber.create()).awaitItem().assertCompleted();

        // When
        final Uni<ProfilePicture> uni = s3ProfilePictureRepository.getLast(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<ProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final ProfilePicture profilePicture = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(profilePicture.userPseudo().pseudo()).isEqualTo("user");
        assertThat(profilePicture.picture()).isEqualTo("picture".getBytes());
        assertThat(profilePicture.mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(profilePicture.contentLength()).isEqualTo(7l);
        assertThat(profilePicture.versionId()).isNotNull();

        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("user")
                .build()).versions();
        final ObjectVersion lastObjectVersion = objectVersions.get(0);
        assertThat(lastObjectVersion.versionId()).isEqualTo(profilePicture.versionId());
        assertThat(lastObjectVersion.isLatest()).isEqualTo(true);
    }

    @Test
    public void should_get_last_return_profile_picture_not_available_yet_exception_when_no_picture_has_been_saved() {
        // Given
        final UserPseudo userPseudo = () -> "user";

        // When
        final Uni<ProfilePicture> uni = s3ProfilePictureRepository.getLast(userPseudo, SupportedMediaType.IMAGE_JPEG);

        // Then
        final UniAssertSubscriber<ProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(ProfilePictureNotAvailableYetException.class);
    }

    @Test
    public void should_get_last_return_profile_picture_repository_exception_when_user_pseudo_name_is_invalid() {
        // Given
        final UserPseudo userPseudo = () -> "";
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        doReturn("").when(supportedMediaType).extension();

        // When
        final Uni<ProfilePicture> uni = s3ProfilePictureRepository.getLast(userPseudo, supportedMediaType);

        // Then
        final UniAssertSubscriber<ProfilePicture> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(ProfilePictureRepositoryException.class);
        verify(supportedMediaType, times(1)).extension();
    }
}
