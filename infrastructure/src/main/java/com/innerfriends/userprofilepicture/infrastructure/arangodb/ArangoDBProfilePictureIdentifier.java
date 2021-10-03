package com.innerfriends.userprofilepicture.infrastructure.arangodb;

import com.arangodb.entity.DocumentField;
import com.innerfriends.userprofilepicture.domain.*;

import java.util.Objects;

public final class ArangoDBProfilePictureIdentifier implements UserProfilePictureIdentifier {

    @DocumentField(DocumentField.Type.KEY)
    public String userPseudo;

    public SupportedMediaType mediaType;

    public String versionId;

    public ArangoDBProfilePictureIdentifier() {}

    public ArangoDBProfilePictureIdentifier(final UserProfilePictureIdentifier userProfilePictureIdentifier) {
        this.userPseudo = userProfilePictureIdentifier.userPseudo().pseudo();
        this.mediaType = userProfilePictureIdentifier.mediaType();
        this.versionId = userProfilePictureIdentifier.versionId().version();
    }

    @Override
    public UserPseudo userPseudo() {
        return () -> userPseudo;
    }

    @Override
    public SupportedMediaType mediaType() {
        return mediaType;
    }

    @Override
    public VersionId versionId() {
        return () -> versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArangoDBProfilePictureIdentifier)) return false;
        ArangoDBProfilePictureIdentifier that = (ArangoDBProfilePictureIdentifier) o;
        return Objects.equals(userPseudo, that.userPseudo) &&
                mediaType == that.mediaType &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userPseudo, mediaType, versionId);
    }
}
