package com.innerfriends.userprofilepicture.infrastructure.hazelcast;

import com.innerfriends.userprofilepicture.domain.ProfilePictureIdentifier;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.domain.VersionId;

import java.io.Serializable;
import java.util.Objects;

public final class HazelcastProfilePictureIdentifier implements ProfilePictureIdentifier, Serializable {

    public String userPseudo;
    public SupportedMediaType mediaType;
    public String versionId;

    public HazelcastProfilePictureIdentifier() {}

    public HazelcastProfilePictureIdentifier(final ProfilePictureIdentifier profilePictureIdentifier) {
        this.userPseudo = profilePictureIdentifier.userPseudo().pseudo();
        this.mediaType = profilePictureIdentifier.mediaType();
        this.versionId = profilePictureIdentifier.versionId().version();
    }

    private HazelcastProfilePictureIdentifier(final Builder builder) {
        this.userPseudo = builder.userPseudo;
        this.mediaType = builder.mediaType;
        this.versionId = builder.versionId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        public String userPseudo;
        public SupportedMediaType mediaType;
        public String versionId;

        private Builder() {}

        public Builder setUserPseudo(String userPseudo) {
            this.userPseudo = userPseudo;
            return this;
        }

        public Builder setMediaType(SupportedMediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder setVersionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public HazelcastProfilePictureIdentifier build() {
            return new HazelcastProfilePictureIdentifier(this);
        }

    }

    @Override
    public UserPseudo userPseudo() {
        return () -> userPseudo;
    }

    @Override
    public SupportedMediaType mediaType() {
        return mediaType;
    }

    @Override
    public VersionId versionId() {
        return () -> versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HazelcastProfilePictureIdentifier)) return false;
        HazelcastProfilePictureIdentifier that = (HazelcastProfilePictureIdentifier) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                mediaType == that.mediaType &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, mediaType, versionId);
    }

    @Override
    public String toString() {
        return "HazelcastProfilePictureIdentifier{" +
                "userPseudo='" + userPseudo + '\'' +
                ", mediaType=" + mediaType +
                ", versionId='" + versionId + '\'' +
                '}';
    }
}
