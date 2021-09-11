package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.ContentUserProfilePicture;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.domain.VersionId;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Arrays;
import java.util.Objects;

public final class S3ContentUserProfilePicture implements ContentUserProfilePicture {

    private final UserPseudo userPseudo;
    private final byte[] picture;
    private final SupportedMediaType mediaType;
    private final Long contentLength;
    private final VersionId versionId;

    public S3ContentUserProfilePicture(final UserPseudo userPseudo, final ResponseBytes<GetObjectResponse> getObjectResponse) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
        this.picture = getObjectResponse.asByteArray();
        this.mediaType = SupportedMediaType.fromContentType(getObjectResponse.response().contentType());
        this.contentLength = getObjectResponse.response().contentLength();
        this.versionId = new S3VersionId(getObjectResponse.response().versionId());
    }

    @Override
    public UserPseudo userPseudo() {
        return userPseudo;
    }

    @Override
    public byte[] picture() {
        return picture;
    }

    @Override
    public SupportedMediaType mediaType() {
        return mediaType;
    }

    @Override
    public Long contentLength() {
        return contentLength;
    }

    @Override
    public VersionId versionId() {
        return versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S3ContentUserProfilePicture)) return false;
        S3ContentUserProfilePicture that = (S3ContentUserProfilePicture) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                Arrays.equals(picture, that.picture) &&
                mediaType == that.mediaType &&
                Objects.equals(contentLength, that.contentLength) &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(userPseudo, mediaType, contentLength, versionId);
        result = 31 * result + Arrays.hashCode(picture);
        return result;
    }
}
