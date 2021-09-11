package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.UserProfilePictureIdentifier;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class UserProfilePictureIdentifierDTO {

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
}
