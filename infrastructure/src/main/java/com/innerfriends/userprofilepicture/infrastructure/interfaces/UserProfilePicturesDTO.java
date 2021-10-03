package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.UserProfilePictures;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RegisterForReflection
public final class UserProfilePicturesDTO {

    private final UserProfilePictures.FeatureState featureState;
    private final List<UserProfilePictureDTO> userProfilePictures;

    public UserProfilePicturesDTO(final UserProfilePictures userProfilePictures) {
        this.featureState = userProfilePictures.featureState();
        this.userProfilePictures = userProfilePictures.userProfilePictures().stream()
                .map(UserProfilePictureDTO::new)
                .collect(Collectors.toList());
    }

    public UserProfilePictures.FeatureState getFeatureState() {
        return featureState;
    }

    public List<UserProfilePictureDTO> getUserProfilePictures() {
        return userProfilePictures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfilePicturesDTO)) return false;
        UserProfilePicturesDTO that = (UserProfilePicturesDTO) o;
        return featureState == that.featureState &&
                Objects.equals(userProfilePictures, that.userProfilePictures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureState, userProfilePictures);
    }
}
