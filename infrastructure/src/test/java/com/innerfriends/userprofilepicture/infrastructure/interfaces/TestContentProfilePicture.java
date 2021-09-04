package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.ContentProfilePicture;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.domain.VersionId;

public class TestContentProfilePicture implements ContentProfilePicture {

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
