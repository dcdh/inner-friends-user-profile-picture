package com.innerfriends.userprofilepicture.domain;

import java.util.stream.Stream;

public enum SupportedMediaType {

    IMAGE_JPEG {
        @Override
        public String contentType() {
            return "image/jpeg";
        }

    };

    public abstract String contentType();

    public String extension() {
        return "." + contentType().substring(contentType().lastIndexOf('/') + 1);
    }

    public static SupportedMediaType fromContentType(final String mimeType) {
        return Stream.of(SupportedMediaType.values())
                .filter(supportedMediaType -> mimeType.equals(supportedMediaType.contentType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException());
    }
}
