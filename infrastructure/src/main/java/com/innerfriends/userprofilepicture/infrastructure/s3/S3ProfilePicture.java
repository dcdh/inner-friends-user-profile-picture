package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.ProfilePicture;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Arrays;
import java.util.Objects;

public final class S3ProfilePicture implements ProfilePicture {

    private final UserPseudo userPseudo;
    private final byte[] picture;
    private final SupportedMediaType mediaType;
    private final Long contentLength;
    private final String versionId;

    public S3ProfilePicture(final UserPseudo userPseudo, final ResponseBytes<GetObjectResponse> getObjectResponse) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
        this.picture = getObjectResponse.asByteArray();
        this.mediaType = SupportedMediaType.fromMimeType(getObjectResponse.response().contentType());
        this.contentLength = getObjectResponse.response().contentLength();
        this.versionId = getObjectResponse.response().versionId();
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
    public String versionId() {
        return versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof S3ProfilePicture)) return false;
        S3ProfilePicture that = (S3ProfilePicture) o;
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
