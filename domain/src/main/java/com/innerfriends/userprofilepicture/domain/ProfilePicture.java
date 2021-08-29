package com.innerfriends.userprofilepicture.domain;

public interface ProfilePicture {

    UserPseudo userPseudo();

    byte[] picture();

    SupportedMediaType mediaType();

    Long contentLength();

    String versionId();

}
