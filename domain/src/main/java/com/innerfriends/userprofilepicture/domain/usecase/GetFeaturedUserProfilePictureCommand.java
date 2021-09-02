package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UseCaseCommand;
import com.innerfriends.userprofilepicture.domain.UserPseudo;

import java.util.Objects;

public final class GetFeaturedUserProfilePictureCommand implements UseCaseCommand {

    private final UserPseudo userPseudo;

    private final SupportedMediaType mediaType;

    public GetFeaturedUserProfilePictureCommand(final UserPseudo userPseudo, final SupportedMediaType mediaType) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
        this.mediaType = Objects.requireNonNull(mediaType);
    }

    @Override
    public UserPseudo userPseudo() {
        return userPseudo;
    }

    public SupportedMediaType mediaType() {
        return mediaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GetFeaturedUserProfilePictureCommand)) return false;
        GetFeaturedUserProfilePictureCommand that = (GetFeaturedUserProfilePictureCommand) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                mediaType == that.mediaType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, mediaType);
    }
}
