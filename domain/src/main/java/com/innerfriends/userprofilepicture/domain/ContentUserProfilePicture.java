package com.innerfriends.userprofilepicture.domain;

public interface ContentUserProfilePicture extends UserProfilePictureIdentifier {

    byte[] picture();

    Long contentLength();

}
