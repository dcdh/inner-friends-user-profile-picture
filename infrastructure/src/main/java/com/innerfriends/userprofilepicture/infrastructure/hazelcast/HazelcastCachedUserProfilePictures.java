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
    public List<HazelcastUserProfilePictureIdentifier> userProfilePictureIdentifiers;
    public HazelcastUserProfilePictureIdentifier featuredUserProfilePictureIdentifier;

    public HazelcastCachedUserProfilePictures() {}

    private HazelcastCachedUserProfilePictures(final Builder builder) {
        this.userPseudo = builder.userPseudo;
        this.userProfilePictureIdentifiers = builder.userProfilePictureIdentifiers;
        this.featuredUserProfilePictureIdentifier = builder.featuredUserProfilePictureIdentifier;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {

        public String userPseudo;
        public List<HazelcastUserProfilePictureIdentifier> userProfilePictureIdentifiers = new ArrayList<>();
        public HazelcastUserProfilePictureIdentifier featuredUserProfilePictureIdentifier;

        private Builder() {}

        public Builder setUserPseudo(String userPseudo) {
            this.userPseudo = userPseudo;
            return this;
        }

        public Builder addProfilePictureIdentifier(final HazelcastUserProfilePictureIdentifier userProfilePictureIdentifier) {
            this.userProfilePictureIdentifiers.add(userProfilePictureIdentifier);
            return this;
        }

        public Builder setFeaturedUserProfilePictureIdentifier(final HazelcastUserProfilePictureIdentifier featuredUserProfilePictureIdentifier) {
            this.featuredUserProfilePictureIdentifier = featuredUserProfilePictureIdentifier;
            return this;
        }

        public HazelcastCachedUserProfilePictures build() {
            return new HazelcastCachedUserProfilePictures(this);
        }
    }

    public HazelcastCachedUserProfilePictures replaceAllProfilePictureIdentifiers(final List<HazelcastUserProfilePictureIdentifier> userProfilePictureIdentifiers) {
        this.userProfilePictureIdentifiers = userProfilePictureIdentifiers;
        return this;
    }

    public HazelcastCachedUserProfilePictures setFeaturedUserProfilePictureIdentifier(final HazelcastUserProfilePictureIdentifier featuredUserProfilePictureIdentifier) {
        this.featuredUserProfilePictureIdentifier = featuredUserProfilePictureIdentifier;
        return this;
    }

    @Override
    public UserPseudo userPseudo() {
        return () -> userPseudo;
    }

    @Override
    public List<? extends UserProfilePictureIdentifier> userProfilePictureIdentifiers() {
        return userProfilePictureIdentifiers;
    }

    @Override
    public UserProfilePictureIdentifier featured() {
        return featuredUserProfilePictureIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HazelcastCachedUserProfilePictures)) return false;
        HazelcastCachedUserProfilePictures that = (HazelcastCachedUserProfilePictures) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                Objects.equals(userProfilePictureIdentifiers, that.userProfilePictureIdentifiers) &&
                Objects.equals(featuredUserProfilePictureIdentifier, that.featuredUserProfilePictureIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, userProfilePictureIdentifiers, featuredUserProfilePictureIdentifier);
    }

    @Override
    public String toString() {
        return "HazelcastCachedUserProfilePictures{" +
                "userPseudo='" + userPseudo + '\'' +
                ", userProfilePictureIdentifiers=" + userProfilePictureIdentifiers +
                ", featuredProfilePictureIdentifier=" + featuredUserProfilePictureIdentifier +
                '}';
    }
}
