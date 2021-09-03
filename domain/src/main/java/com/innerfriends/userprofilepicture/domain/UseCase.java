package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

public interface UseCase<R, C extends UseCaseCommand> {

    Uni<R> execute(C command, ResponseTransformer<R> responseTransformer);

}
