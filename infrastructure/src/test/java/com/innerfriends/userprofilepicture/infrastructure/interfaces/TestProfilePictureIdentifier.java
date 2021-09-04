package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ProfilePictureIdentifier;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.domain.VersionId;

public class TestProfilePictureIdentifier implements ProfilePictureIdentifier {

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
