package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import java.util.Objects;

public final class ImageContentType {

    private final String imageContentType;

    // image/jpeg; charset=ISO-8859-1
    public ImageContentType(final String contentType) {
        Objects.requireNonNull(contentType);
        if (!contentType.matches(".*image.*;.*charset.*")) {
            throw new IllegalStateException();
        }
        this.imageContentType = contentType.substring(0, contentType.indexOf(';'));
    }

    public String imageContentType() {
        return imageContentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageContentType)) return false;
        ImageContentType that = (ImageContentType) o;
        return Objects.equals(imageContentType, that.imageContentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageContentType);
    }
}
