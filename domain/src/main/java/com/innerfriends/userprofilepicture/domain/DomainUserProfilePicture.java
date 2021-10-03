package com.innerfriends.userprofilepicture.domain;

import java.util.Objects;

public final class DomainUserProfilePicture implements UserProfilePicture {

    private final UserPseudo userPseudo;
    private final SupportedMediaType mediaType;
    private final VersionId versionId;
    private final boolean isFeatured;

    public DomainUserProfilePicture(final UserProfilePictureIdentifier userProfilePictureIdentifier, final Boolean isFeatured) {
        this.userPseudo = Objects.requireNonNull(userProfilePictureIdentifier.userPseudo());
        this.mediaType = Objects.requireNonNull(userProfilePictureIdentifier.mediaType());
        this.versionId = Objects.requireNonNull(userProfilePictureIdentifier.versionId());
        this.isFeatured = Objects.requireNonNull(isFeatured);
    }

    public DomainUserProfilePicture(final UserProfilePictureIdentifier userProfilePictureIdentifier,
                                    final UserProfilePictureIdentifier featured) {
        this(userProfilePictureIdentifier, userProfilePictureIdentifier.equals(featured));
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
    public boolean isFeatured() {
        return isFeatured;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainUserProfilePicture)) return false;
        DomainUserProfilePicture that = (DomainUserProfilePicture) o;
        return isFeatured == that.isFeatured &&
                Objects.equals(userPseudo, that.userPseudo) &&
                mediaType == that.mediaType &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, mediaType, versionId, isFeatured);
    }

    @Override
    public String toString() {
        return "DomainUserProfilePicture{" +
                "userPseudo=" + userPseudo +
                ", mediaType=" + mediaType +
                ", versionId=" + versionId +
                ", isFeatured=" + isFeatured +
                '}';
    }
}
