package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ImageContentTypeTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(ImageContentType.class).verify();
    }

    @Test
    public void should_fail_fast_when_content_type_is_null() {
        assertThatThrownBy(() -> new ImageContentType(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_content_type_is_invalid() {
        assertThatThrownBy(() -> new ImageContentType("boom"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_return_image_content_type() {
        assertThat(new ImageContentType("image/jpeg; charset=ISO-8859-1").imageContentType())
                .isEqualTo("image/jpeg");
    }
}
