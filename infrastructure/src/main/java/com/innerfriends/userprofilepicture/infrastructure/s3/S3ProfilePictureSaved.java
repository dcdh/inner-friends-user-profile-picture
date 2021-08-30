package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.ProfilePictureSaved;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.Objects;

public final class S3ProfilePictureSaved implements ProfilePictureSaved {

    private final UserPseudo userPseudo;
    private final String versionId;

    public S3ProfilePictureSaved(final UserPseudo userPseudo, final PutObjectResponse putObjectResponse) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
        this.versionId = Objects.requireNonNull(putObjectResponse).versionId();
    }

    @Override
    public UserPseudo userPseudo() {
        return userPseudo;
    }

    @Override
    public String versionId() {
        return versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S3ProfilePictureSaved)) return false;
        S3ProfilePictureSaved that = (S3ProfilePictureSaved) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, versionId);
    }
}
