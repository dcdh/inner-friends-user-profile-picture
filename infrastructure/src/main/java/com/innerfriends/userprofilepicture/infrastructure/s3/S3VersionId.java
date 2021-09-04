package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.VersionId;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

import java.util.Objects;

public final class S3VersionId implements VersionId {

    private final String version;

    public S3VersionId(final String version) {
        this.version = Objects.requireNonNull(version);
    }

    public S3VersionId(final ObjectVersion objectVersion) {
        this(objectVersion.versionId());
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S3VersionId)) return false;
        S3VersionId that = (S3VersionId) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}
