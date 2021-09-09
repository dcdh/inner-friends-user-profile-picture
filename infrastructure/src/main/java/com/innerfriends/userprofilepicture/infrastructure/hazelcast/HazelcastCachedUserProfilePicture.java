package com.innerfriends.userprofilepicture.infrastructure.hazelcast;

import com.innerfriends.userprofilepicture.domain.CachedUserProfilePicture;
import com.innerfriends.userprofilepicture.domain.ProfilePictureIdentifier;
import com.innerfriends.userprofilepicture.domain.UserPseudo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HazelcastCachedUserProfilePicture implements CachedUserProfilePicture, Serializable {

    public String userPseudo;
    public List<HazelcastProfilePictureIdentifier> profilePictureIdentifiers;
    public HazelcastProfilePictureIdentifier featuredProfilePictureIdentifier;

    public HazelcastCachedUserProfilePicture() {}

    private HazelcastCachedUserProfilePicture(final Builder builder) {
        this.userPseudo = builder.userPseudo;
        this.profilePictureIdentifiers = builder.profilePictureIdentifiers;
        this.featuredProfilePictureIdentifier = builder.featuredProfilePictureIdentifier;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        public String userPseudo;
        public List<HazelcastProfilePictureIdentifier> profilePictureIdentifiers = new ArrayList<>();
        public HazelcastProfilePictureIdentifier featuredProfilePictureIdentifier;

        private Builder() {}

        public Builder setUserPseudo(String userPseudo) {
            this.userPseudo = userPseudo;
            return this;
        }

        public Builder addProfilePictureIdentifier(final HazelcastProfilePictureIdentifier profilePictureIdentifier) {
            this.profilePictureIdentifiers.add(profilePictureIdentifier);
            return this;
        }

        public Builder setFeaturedProfilePictureIdentifier(final HazelcastProfilePictureIdentifier featuredProfilePictureIdentifier) {
            this.featuredProfilePictureIdentifier = featuredProfilePictureIdentifier;
            return this;
        }

        public HazelcastCachedUserProfilePicture build() {
            return new HazelcastCachedUserProfilePicture(this);
        }
    }

    public HazelcastCachedUserProfilePicture replaceAllProfilePictureIdentifiers(final List<HazelcastProfilePictureIdentifier> profilePictureIdentifiers) {
        this.profilePictureIdentifiers = profilePictureIdentifiers;
        return this;
    }

    public HazelcastCachedUserProfilePicture setFeaturedProfilePictureIdentifier(final HazelcastProfilePictureIdentifier featuredProfilePictureIdentifier) {
        this.featuredProfilePictureIdentifier = featuredProfilePictureIdentifier;
        return this;
    }

    @Override
    public UserPseudo userPseudo() {
        return () -> userPseudo;
    }

    @Override
    public List<? extends ProfilePictureIdentifier> profilePictureIdentifiers() {
        return profilePictureIdentifiers;
    }

    @Override
    public ProfilePictureIdentifier featured() {
        return featuredProfilePictureIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HazelcastCachedUserProfilePicture)) return false;
        HazelcastCachedUserProfilePicture that = (HazelcastCachedUserProfilePicture) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                Objects.equals(profilePictureIdentifiers, that.profilePictureIdentifiers) &&
                Objects.equals(featuredProfilePictureIdentifier, that.featuredProfilePictureIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, profilePictureIdentifiers, featuredProfilePictureIdentifier);
    }

    @Override
    public String toString() {
        return "HazelcastCachedUserProfilePicture{" +
                "userPseudo='" + userPseudo + '\'' +
                ", profilePictureIdentifiers=" + profilePictureIdentifiers +
                ", featuredProfilePictureIdentifier=" + featuredProfilePictureIdentifier +
                '}';
    }
}
