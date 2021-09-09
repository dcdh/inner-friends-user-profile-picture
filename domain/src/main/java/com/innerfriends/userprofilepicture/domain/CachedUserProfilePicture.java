package com.innerfriends.userprofilepicture.domain;

import java.util.List;

public interface CachedUserProfilePicture {

    UserPseudo userPseudo();

    List<? extends ProfilePictureIdentifier> profilePictureIdentifiers();

    ProfilePictureIdentifier featured();

}
