package com.innerfriends.userprofilepicture.infrastructure.s3;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class S3VersionIdTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(S3VersionId.class).verify();
    }

    @Test
    public void should_fail_fast_when_object_version_is_null() {
        assertThatThrownBy(() -> new S3VersionId((ObjectVersion) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_version_is_null() {
        assertThatThrownBy(() -> new S3VersionId((String) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_version() {
        // Given
        final ObjectVersion givenObjectVersion = mock(ObjectVersion.class);
        doReturn("versionId").when(givenObjectVersion).versionId();

        // When && Then
        assertThat(new S3VersionId(givenObjectVersion).version())
                .isEqualTo("versionId");
        verify(givenObjectVersion, times(1)).versionId();
    }
}
