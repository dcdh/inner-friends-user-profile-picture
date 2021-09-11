package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;

public class TestUserProfilePictureIdentifier implements UserProfilePictureIdentifier {

    private final String versionId;

    public TestUserProfilePictureIdentifier(final String versionId) {
        this.versionId = versionId;
    }

    @Override
    public UserPseudo userPseudo() {
        return () -> "user";
    }

    @Override
    public SupportedMediaType mediaType() {
        return SupportedMediaType.IMAGE_JPEG;
    }

    @Override
    public VersionId versionId() {
        return () -> versionId;
    }
}
