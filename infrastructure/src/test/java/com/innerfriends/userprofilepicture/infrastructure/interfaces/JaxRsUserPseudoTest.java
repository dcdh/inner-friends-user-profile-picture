package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class JaxRsUserPseudoTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(JaxRsUserPseudo.class).verify();
    }

    @Test
    public void should_fail_fast_when_pseudo_is_null() {
        assertThatThrownBy(() -> new JaxRsUserPseudo(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_pseudo() {
        assertThat(new JaxRsUserPseudo("pseudo").pseudo())
                .isEqualTo("pseudo");
    }

}
