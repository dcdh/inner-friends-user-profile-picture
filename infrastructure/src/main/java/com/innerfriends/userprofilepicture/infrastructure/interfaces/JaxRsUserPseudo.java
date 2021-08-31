package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.UserPseudo;

import java.util.Objects;

public final class JaxRsUserPseudo implements UserPseudo {

    private final String pseudo;

    public JaxRsUserPseudo(final String pseudo) {
        this.pseudo = Objects.requireNonNull(pseudo);
    }

    @Override
    public String pseudo() {
        return pseudo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JaxRsUserPseudo)) return false;
        JaxRsUserPseudo that = (JaxRsUserPseudo) o;
        return Objects.equals(pseudo, that.pseudo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pseudo);
    }
}
