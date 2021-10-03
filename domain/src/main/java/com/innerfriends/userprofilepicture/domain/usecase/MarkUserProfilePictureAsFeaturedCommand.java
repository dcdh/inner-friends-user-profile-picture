package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;

import java.util.Objects;

public final class MarkUserProfilePictureAsFeaturedCommand implements UserProfilePictureIdentifier, UseCaseCommand {

    private final UserPseudo userPseudo;

    private final SupportedMediaType mediaType;

    private final VersionId versionId;

    public MarkUserProfilePictureAsFeaturedCommand(final UserPseudo userPseudo,
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
        if (!(o instanceof MarkUserProfilePictureAsFeaturedCommand)) return false;
        MarkUserProfilePictureAsFeaturedCommand that = (MarkUserProfilePictureAsFeaturedCommand) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                mediaType == that.mediaType &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, mediaType, versionId);
    }
}
