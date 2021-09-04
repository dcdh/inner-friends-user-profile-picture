package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;

import java.util.Objects;

public final class GetUserProfilePictureByVersionCommand implements ProfilePictureIdentifier, UseCaseCommand {

    private final UserPseudo userPseudo;

    private final SupportedMediaType mediaType;

    private final VersionId versionId;

    public GetUserProfilePictureByVersionCommand(final UserPseudo userPseudo,
                                                 final SupportedMediaType mediaType,
                                                 final VersionId versionId) {
        this.userPseudo = Objects.requireNonNull(userPseudo);
        this.mediaType = Objects.requireNonNull(mediaType);
        this.versionId = Objects.requireNonNull(versionId);
    }

    @Override
    public UserPseudo userPseudo() {
        return userPseudo;
    }

    @Override
    public SupportedMediaType mediaType() {
        return mediaType;
    }

    @Override
    public VersionId versionId() {
        return versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GetUserProfilePictureByVersionCommand)) return false;
        GetUserProfilePictureByVersionCommand that = (GetUserProfilePictureByVersionCommand) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                mediaType == that.mediaType &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, mediaType, versionId);
    }
}
