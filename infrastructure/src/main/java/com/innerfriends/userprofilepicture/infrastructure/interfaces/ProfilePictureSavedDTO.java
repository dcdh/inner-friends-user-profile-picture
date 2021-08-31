package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ProfilePictureSaved;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ProfilePictureSavedDTO {

    private final String userPseudo;
    private final String versionId;

    public ProfilePictureSavedDTO(final ProfilePictureSaved profilePictureSaved) {
        this.userPseudo = profilePictureSaved.userPseudo().pseudo();
        this.versionId = profilePictureSaved.versionId();
    }

    public String getUserPseudo() {
        return userPseudo;
    }

    public String getVersionId() {
        return versionId;
    }

}
