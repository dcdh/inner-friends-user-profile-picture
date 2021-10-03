package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserProfilePictureIdentifier;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Objects;

@RegisterForReflection
public final class UserProfilePictureIdentifierDTO {

    private final String userPseudo;
    private final SupportedMediaType mediaType;
    private final String versionId;

    public UserProfilePictureIdentifierDTO(final UserProfilePictureIdentifier userProfilePictureIdentifier) {
        this.userPseudo = userProfilePictureIdentifier.userPseudo().pseudo();
        this.mediaType = userProfilePictureIdentifier.mediaType();
        this.versionId = userProfilePictureIdentifier.versionId().version();
    }

    public String getUserPseudo() {
        return userPseudo;
    }

    public SupportedMediaType getMediaType() {
        return mediaType;
    }

    public String getVersionId() {
        return versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfilePictureIdentifierDTO)) return false;
        UserProfilePictureIdentifierDTO that = (UserProfilePictureIdentifierDTO) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                mediaType == that.mediaType &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, mediaType, versionId);
    }
}
