package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ProfilePictureSaved;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.domain.VersionId;

public class TestProfilePictureSaved implements ProfilePictureSaved {

    @Override
    public UserPseudo userPseudo() {
        return () -> "pseudo";
    }

    @Override
    public SupportedMediaType mediaType() {
        return SupportedMediaType.IMAGE_JPEG;
    }

    @Override
    public VersionId versionId() {
        return () -> "v0";
    }
}