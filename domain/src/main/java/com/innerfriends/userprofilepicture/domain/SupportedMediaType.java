package com.innerfriends.userprofilepicture.domain;

public enum SupportedMediaType {

    IMAGE_JPEG {
        @Override
        String mimeType() {
            return "image/jpeg";
        }
    };

    abstract String mimeType();

}
