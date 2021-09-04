package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.VersionId;

import java.util.Objects;

public final class JaxRsVersionId implements VersionId {

    private final String version;

    public JaxRsVersionId(final String version) {
        this.version = Objects.requireNonNull(version);
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JaxRsVersionId)) return false;
        JaxRsVersionId that = (JaxRsVersionId) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }
}
