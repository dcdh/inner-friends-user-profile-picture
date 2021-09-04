package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.ProfilePictureIdentifier;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.domain.VersionId;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

import java.util.Objects;

public class S3ProfilePictureIdentifier implements ProfilePictureIdentifier {

    private final UserPseudo userPseudo;

    private final SupportedMediaType mediaType;

    private final VersionId versionId;

    public S3ProfilePictureIdentifier(final UserPseudo userPseudo,
                                      final SupportedMediaType mediaType,
                                      final ObjectVersion objectVersion) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
        this.mediaType = Objects.requireNonNull(mediaType);
        this.versionId = new S3VersionId(objectVersion);
    }

    @Override
    public UserPseudo userPseudo() {
        return userPseudo;
    }

    @Override
    public SupportedMediaType mediaType() {
        return mediaType;
    }

    @Override
    public VersionId versionId() {
        return versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S3ProfilePictureIdentifier)) return false;
        S3ProfilePictureIdentifier that = (S3ProfilePictureIdentifier) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                mediaType == that.mediaType &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, mediaType, versionId);
    }
}
