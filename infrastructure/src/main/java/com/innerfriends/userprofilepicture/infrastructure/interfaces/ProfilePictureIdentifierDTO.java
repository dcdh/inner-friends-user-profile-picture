package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ProfilePictureIdentifier;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ProfilePictureIdentifierDTO {

    private final String userPseudo;
    private final SupportedMediaType mediaType;
    private final String versionId;

    public ProfilePictureIdentifierDTO(final ProfilePictureIdentifier profilePictureIdentifier) {
        this.userPseudo = profilePictureIdentifier.userPseudo().pseudo();
        this.mediaType = profilePictureIdentifier.mediaType();
        this.versionId = profilePictureIdentifier.versionId().version();
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
