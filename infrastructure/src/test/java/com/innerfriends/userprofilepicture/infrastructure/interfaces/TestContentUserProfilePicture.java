package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ContentUserProfilePicture;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.domain.VersionId;

public class TestContentUserProfilePicture implements ContentUserProfilePicture {

    @Override
    public UserPseudo userPseudo() {
        return () -> "pseudo";
    }

    @Override
    public byte[] picture() {
        return "picture".getBytes();
    }

    @Override
    public SupportedMediaType mediaType() {
        return SupportedMediaType.IMAGE_JPEG;
    }

    @Override
    public Long contentLength() {
        return 7L;
    }

    @Override
    public VersionId versionId() {
        return () -> "v0";
    }
}
