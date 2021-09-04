package com.innerfriends.userprofilepicture.domain;

public interface ProfilePictureIdentifier {

    UserPseudo userPseudo();

    SupportedMediaType mediaType();

    VersionId versionId();

}
