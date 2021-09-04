package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;

public interface S3ObjectKeyProvider {

    ObjectKey objectKey(UserPseudo userPseudo, SupportedMediaType mediaType);

}
