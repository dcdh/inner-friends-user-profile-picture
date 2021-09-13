package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

public interface LockMechanism {

    Uni<Void> lock(UserPseudo userPseudo);

    Uni<Void> unlock(UserPseudo userPseudo);

}
