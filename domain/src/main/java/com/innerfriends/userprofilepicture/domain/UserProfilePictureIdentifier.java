package com.innerfriends.userprofilepicture.domain;

public interface UserProfilePictureIdentifier {

    UserPseudo userPseudo();

    SupportedMediaType mediaType();

    VersionId versionId();

}
