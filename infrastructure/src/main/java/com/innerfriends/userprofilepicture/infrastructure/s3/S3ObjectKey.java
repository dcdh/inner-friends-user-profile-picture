package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;

import java.util.Objects;

public final class S3ObjectKey {

    private final UserPseudo userPseudo;
    private final SupportedMediaType supportedMediaType;

    public S3ObjectKey(final UserPseudo userPseudo, final SupportedMediaType supportedMediaType) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
        this.supportedMediaType = Objects.requireNonNull(supportedMediaType);
    }

    public String objectKey() {
        return this.userPseudo.pseudo() + this.supportedMediaType.extension();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S3ObjectKey)) return false;
        S3ObjectKey that = (S3ObjectKey) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                supportedMediaType == that.supportedMediaType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, supportedMediaType);
    }
}
