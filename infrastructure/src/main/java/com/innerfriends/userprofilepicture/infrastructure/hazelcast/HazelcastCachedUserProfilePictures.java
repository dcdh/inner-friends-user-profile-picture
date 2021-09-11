package com.innerfriends.userprofilepicture.infrastructure.hazelcast;

import com.innerfriends.userprofilepicture.domain.CachedUserProfilePictures;
import com.innerfriends.userprofilepicture.domain.UserProfilePictureIdentifier;
import com.innerfriends.userprofilepicture.domain.UserPseudo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HazelcastCachedUserProfilePictures implements CachedUserProfilePictures, Serializable {

    public String userPseudo;
    public List<HazelcastUserProfilePictureIdentifier> profilePictureIdentifiers;
    public HazelcastUserProfilePictureIdentifier featuredProfilePictureIdentifier;

    public HazelcastCachedUserProfilePictures() {}

    private HazelcastCachedUserProfilePictures(final Builder builder) {
        this.userPseudo = builder.userPseudo;
        this.profilePictureIdentifiers = builder.profilePictureIdentifiers;
        this.featuredProfilePictureIdentifier = builder.featuredProfilePictureIdentifier;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        public String userPseudo;
        public List<HazelcastUserProfilePictureIdentifier> profilePictureIdentifiers = new ArrayList<>();
        public HazelcastUserProfilePictureIdentifier featuredProfilePictureIdentifier;

        private Builder() {}

        public Builder setUserPseudo(String userPseudo) {
            this.userPseudo = userPseudo;
            return this;
        }

        public Builder addProfilePictureIdentifier(final HazelcastUserProfilePictureIdentifier profilePictureIdentifier) {
            this.profilePictureIdentifiers.add(profilePictureIdentifier);
            return this;
        }

        public Builder setFeaturedProfilePictureIdentifier(final HazelcastUserProfilePictureIdentifier featuredProfilePictureIdentifier) {
            this.featuredProfilePictureIdentifier = featuredProfilePictureIdentifier;
            return this;
        }

        public HazelcastCachedUserProfilePictures build() {
            return new HazelcastCachedUserProfilePictures(this);
        }
    }

    public HazelcastCachedUserProfilePictures replaceAllProfilePictureIdentifiers(final List<HazelcastUserProfilePictureIdentifier> profilePictureIdentifiers) {
        this.profilePictureIdentifiers = profilePictureIdentifiers;
        return this;
    }

    public HazelcastCachedUserProfilePictures setFeaturedProfilePictureIdentifier(final HazelcastUserProfilePictureIdentifier featuredProfilePictureIdentifier) {
        this.featuredProfilePictureIdentifier = featuredProfilePictureIdentifier;
        return this;
    }

    @Override
    public UserPseudo userPseudo() {
        return () -> userPseudo;
    }

    @Override
    public List<? extends UserProfilePictureIdentifier> profilePictureIdentifiers() {
        return profilePictureIdentifiers;
    }

    @Override
    public UserProfilePictureIdentifier featured() {
        return featuredProfilePictureIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HazelcastCachedUserProfilePictures)) return false;
        HazelcastCachedUserProfilePictures that = (HazelcastCachedUserProfilePictures) o;
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
