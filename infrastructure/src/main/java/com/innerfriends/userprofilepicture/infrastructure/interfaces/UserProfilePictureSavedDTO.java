package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.UserProfilePictureSaved;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class UserProfilePictureSavedDTO {

    private final String userPseudo;
    private final SupportedMediaType mediaType;
    private final String versionId;

    public UserProfilePictureSavedDTO(final UserProfilePictureSaved userProfilePictureSaved) {
        this.userPseudo = userProfilePictureSaved.userPseudo().pseudo();
        this.mediaType = userProfilePictureSaved.mediaType();
        this.versionId = userProfilePictureSaved.versionId().version();
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
