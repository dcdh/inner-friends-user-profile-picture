package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SupportedMediaTypeTest {

    @Test
    public void should_image_jpeg_return_image_jpeg_content_type() {
        assertThat(SupportedMediaType.IMAGE_JPEG.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    public void should_image_jpeg_return_jpeg_extension() {
        assertThat(SupportedMediaType.IMAGE_JPEG.extension()).isEqualTo(".jpeg");
    }

    @Test
    public void should_build_supported_media_type_from_image_jpeg() {
        assertThat(SupportedMediaType.fromContentType("image/jpeg")).isEqualTo(SupportedMediaType.IMAGE_JPEG);
    }

    @Test
    public void should_throw_exception_on_unsupported_media_type() {
        assertThatThrownBy(() -> SupportedMediaType.fromContentType("boom"))
                .isInstanceOf(IllegalStateException.class);
    }

}
