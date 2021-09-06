package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;

public class TestProfilePictureIdentifier implements ProfilePictureIdentifier {

    private final String versionId;

    public TestProfilePictureIdentifier(final String versionId) {
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
