package com.innerfriends.userprofilepicture.domain;

public interface ContentProfilePicture extends ProfilePictureIdentifier {

    byte[] picture();

    Long contentLength();

}
