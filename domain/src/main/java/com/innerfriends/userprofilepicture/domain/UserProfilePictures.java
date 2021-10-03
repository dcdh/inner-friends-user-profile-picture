package com.innerfriends.userprofilepicture.domain;

import java.util.List;

public interface UserProfilePictures {

    enum FeatureState {

        NOT_SELECTED_YET {

            @Override
            public boolean canBeStoredInCache() {
                return true;
            }

        },

        SELECTED {

            @Override
            public boolean canBeStoredInCache() {
                return true;
            }

        },

        IN_ERROR_WHEN_RETRIEVING {
            @Override
            public boolean canBeStoredInCache() {
                return false;
            }
        };

        public abstract boolean canBeStoredInCache();

    }

    FeatureState featureState();

    List<? extends UserProfilePicture> userProfilePictures();

    default boolean canBeStoredInCache() {
        return featureState().canBeStoredInCache();
    }

}
