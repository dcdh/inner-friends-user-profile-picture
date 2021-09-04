package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.*;

public class TestProfilePictureIdentifier implements ProfilePictureIdentifier {

    private final ProfilePictureSaved profilePictureSaved;

    public TestProfilePictureIdentifier(final ProfilePictureSaved profilePictureSaved) {
        this.profilePictureSaved = profilePictureSaved;
    }

    @Override
    public UserPseudo userPseudo() {
        return profilePictureSaved.userPseudo();
    }

    @Override
    public SupportedMediaType mediaType() {
        return profilePictureSaved.mediaType();
    }

    @Override
    public VersionId versionId() {
        return profilePictureSaved.versionId();
    }
}
