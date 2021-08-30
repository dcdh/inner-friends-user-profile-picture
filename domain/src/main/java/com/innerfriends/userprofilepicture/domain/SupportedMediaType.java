package com.innerfriends.userprofilepicture.domain;

import java.util.stream.Stream;

public enum SupportedMediaType {

    IMAGE_JPEG {
        @Override
        public String mimeType() {
            return "image/jpeg";
        }
    };

    public abstract String mimeType();

    public static SupportedMediaType fromMimeType(final String mimeType) {
        return Stream.of(SupportedMediaType.values())
                .filter(supportedMediaType -> mimeType.equals(supportedMediaType.mimeType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException());
    }
}
