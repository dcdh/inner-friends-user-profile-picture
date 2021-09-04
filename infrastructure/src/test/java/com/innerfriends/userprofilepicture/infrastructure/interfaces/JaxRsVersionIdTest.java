package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class JaxRsVersionIdTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(JaxRsVersionId.class).verify();
    }

    @Test
    public void should_fail_fast_when_version_is_null() {
        assertThatThrownBy(() -> new JaxRsVersionId(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_version() {
        assertThat(new JaxRsVersionId("v0").version())
                .isEqualTo("v0");
    }
}
