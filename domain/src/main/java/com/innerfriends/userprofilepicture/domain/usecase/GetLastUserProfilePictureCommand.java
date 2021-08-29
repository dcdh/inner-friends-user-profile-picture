package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.UseCaseCommand;
import com.innerfriends.userprofilepicture.domain.UserPseudo;

import java.util.Objects;

public final class GetLastUserProfilePictureCommand implements UseCaseCommand {

    private final UserPseudo userPseudo;

    public GetLastUserProfilePictureCommand(final UserPseudo userPseudo) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
    }

    @Override
    public UserPseudo userPseudo() {
        return userPseudo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GetLastUserProfilePictureCommand)) return false;
        GetLastUserProfilePictureCommand that = (GetLastUserProfilePictureCommand) o;
        return Objects.equals(userPseudo, that.userPseudo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo);
    }
}
