package com.innerfriends.userprofilepicture.domain;

import io.smallrye.mutiny.Uni;

public interface UseCase<R, C extends UseCaseCommand, RT extends ResponseTransformer<R>> {

    Uni<R> execute(C command, RT responseTransformer);

}
