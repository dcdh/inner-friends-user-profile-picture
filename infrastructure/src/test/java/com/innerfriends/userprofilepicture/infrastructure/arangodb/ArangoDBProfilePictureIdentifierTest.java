package com.innerfriends.userprofilepicture.infrastructure.arangodb;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

public class ArangoDBProfilePictureIdentifierTest {

    @Test
    public void should_verify_partial_equality() {
        EqualsVerifier.forClass(ArangoDBProfilePictureIdentifier.class)
                .suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
