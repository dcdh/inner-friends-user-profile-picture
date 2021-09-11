package com.innerfriends.userprofilepicture.domain;

import java.util.List;

public interface CachedUserProfilePictures {

    UserPseudo userPseudo();

    List<? extends ProfilePictureIdentifier> profilePictureIdentifiers();

    ProfilePictureIdentifier featured();

}
