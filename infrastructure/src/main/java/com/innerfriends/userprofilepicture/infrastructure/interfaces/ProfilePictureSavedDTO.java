package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ProfilePictureSaved;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ProfilePictureSavedDTO {

    private final String userPseudo;
    private final SupportedMediaType mediaType;
    private final String versionId;

    public ProfilePictureSavedDTO(final ProfilePictureSaved profilePictureSaved) {
        this.userPseudo = profilePictureSaved.userPseudo().pseudo();
        this.mediaType = profilePictureSaved.mediaType();
        this.versionId = profilePictureSaved.versionId().version();
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
