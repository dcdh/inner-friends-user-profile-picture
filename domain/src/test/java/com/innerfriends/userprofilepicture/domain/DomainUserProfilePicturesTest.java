package com.innerfriends.userprofilepicture.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class DomainUserProfilePicturesTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(DomainUserProfilePictures.class).verify();
    }

    @Test
    public void should_fail_fast_when_building_without_selecting_feature_state() {
        assertThatThrownBy(() -> DomainUserProfilePictures.newBuilder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_build_with_featured_state_not_selected_yet() {
        // Given

        // When
        final DomainUserProfilePictures actual = DomainUserProfilePictures.newBuilder()
                .withFeaturedStateNotSelectedYet(buildUserProfilePictureIdentifiers())
                .build();

        // Then
        assertThat(actual.featureState()).isEqualTo(UserProfilePictures.FeatureState.NOT_SELECTED_YET);
        assertThat(actual.userProfilePictures()).isEqualTo(
                List.of(
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v1"), false),
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v2"), false),
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v3"), true)
                )
        );
    }

    @Test
    public void should_build_with_featured_state_selected() {
        // Given

        // When
        final DomainUserProfilePictures actual = DomainUserProfilePictures.newBuilder()
                .withFeaturedStateSelected(buildUserProfilePictureIdentifiers(),
                        new TestUserProfilePictureIdentifier("v2"))
                .build();

        // Then
        assertThat(actual.featureState()).isEqualTo(UserProfilePictures.FeatureState.SELECTED);
        assertThat(actual.userProfilePictures()).isEqualTo(
                List.of(
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v1"), false),
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v2"), true),
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v3"), false)
                )
        );
    }

    @Test
    public void should_build_with_featured_state_in_error_when_retrieving() {
        // Given

        // When
        final DomainUserProfilePictures actual = DomainUserProfilePictures.newBuilder()
                .withFeaturedStateInErrorWhenRetrieving(buildUserProfilePictureIdentifiers())
                .build();

        // Then
        assertThat(actual.featureState()).isEqualTo(UserProfilePictures.FeatureState.IN_ERROR_WHEN_RETRIEVING);
        assertThat(actual.userProfilePictures()).isEqualTo(
                List.of(
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v1"), false),
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v2"), false),
                        new DomainUserProfilePicture(new TestUserProfilePictureIdentifier("v3"), false)
                )
        );
    }

    private List<UserProfilePictureIdentifier> buildUserProfilePictureIdentifiers() {
        return List.of(new TestUserProfilePictureIdentifier("v1"),
                new TestUserProfilePictureIdentifier("v2"),
                new TestUserProfilePictureIdentifier("v3"));
    }

    private final class TestVersionId implements VersionId {

        private final String versionId;

        public TestVersionId(final String versionId) {
            this.versionId = versionId;
        }

        @Override
        public String version() {
            return versionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestVersionId)) return false;
            TestVersionId that = (TestVersionId) o;
            return Objects.equals(versionId, that.versionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(versionId);
        }
    }

    private final class TestUserProfilePictureIdentifier implements UserProfilePictureIdentifier {

        private final VersionId versionId;

        public TestUserProfilePictureIdentifier(final String versionId) {
            this.versionId = new TestVersionId(versionId);
        }

        @Override
        public UserPseudo userPseudo() {
            return () -> "pseudo";
        }

        @Override
        public SupportedMediaType mediaType() {
            return SupportedMediaType.IMAGE_JPEG;
        }

        @Override
        public VersionId versionId() {
            return versionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestUserProfilePictureIdentifier)) return false;
            TestUserProfilePictureIdentifier that = (TestUserProfilePictureIdentifier) o;
            return Objects.equals(versionId, that.versionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(versionId);
        }
    }
}
