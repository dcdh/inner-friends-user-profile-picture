package com.innerfriends.userprofilepicture.infrastructure.arangodb;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.async.ArangoDBAsync;
import com.arangodb.async.ArangoDatabaseAsync;
import com.arangodb.model.DocumentCreateOptions;
import com.arangodb.model.OverwriteMode;
import com.innerfriends.userprofilepicture.domain.*;
import com.innerfriends.userprofilepicture.infrastructure.tracing.OpenTelemetryTracingService;
import io.opentelemetry.api.trace.Span;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Objects;

@ApplicationScoped
public class ArangodbUserProfilPictureFeaturedRepository implements UserProfilPictureFeaturedRepository {

    public static final String COLLECTION_FEATURE = "FEATURE";

    private final ArangoDB arangoDB;
    private final ArangoDBAsync arangoDBAsync;
    private final String dbName;
    private final OpenTelemetryTracingService openTelemetryTracingService;
    private final ManagedExecutor managedExecutor;
    private ArangoDatabaseAsync arangoDatabaseAsync;

    private static final Logger LOG = Logger.getLogger(ArangodbUserProfilPictureFeaturedRepository.class);

    public ArangodbUserProfilPictureFeaturedRepository(final ArangoDBAsync arangoDBAsync,
                                                       final ArangoDB arangoDB,
                                                       @ConfigProperty(name = "arangodb.dbName") final String dbName,
                                                       final OpenTelemetryTracingService openTelemetryTracingService,
                                                       final ManagedExecutor managedExecutor) {
        this.arangoDBAsync = Objects.requireNonNull(arangoDBAsync);
        this.arangoDB = Objects.requireNonNull(arangoDB);
        this.dbName = Objects.requireNonNull(dbName);
        this.openTelemetryTracingService = Objects.requireNonNull(openTelemetryTracingService);
        this.managedExecutor = Objects.requireNonNull(managedExecutor);
    }

    @Override
    public Uni<UserProfilePictureIdentifier> getFeatured(final UserPseudo userPseudo) throws NoUserProfilPictureFeaturedYetException, UserProfilPictureFeaturedRepositoryException {
        final Span span = openTelemetryTracingService.startANewSpan("ArangodbUserProfilPictureFeaturedRepository.getFeatured");
        return Uni.createFrom()
                .completionStage(() -> arangoDatabaseAsync.collection(COLLECTION_FEATURE).getDocument(userPseudo.pseudo(), ArangoDBProfilePictureIdentifier.class))
                .onItem()
                .castTo(UserProfilePictureIdentifier.class)
                .replaceIfNullWith(() -> {
                    throw new NoUserProfilPictureFeaturedYetException(userPseudo);
                })
                .onFailure(ArangoDBException.class)
                .transform(exception -> {
                    LOG.error(exception);
                    openTelemetryTracingService.markSpanInError(span);
                    return new UserProfilPictureFeaturedRepositoryException();
                })
                .onTermination()
                .invoke(() -> openTelemetryTracingService.endSpan(span));
    }

    @Override
    public Uni<UserProfilePictureIdentifier> markAsFeatured(final UserProfilePictureIdentifier userProfilePictureIdentifier)
            throws UserProfilPictureFeaturedRepositoryException {
        final Span span = openTelemetryTracingService.startANewSpan("ArangodbUserProfilPictureFeaturedRepository.markAsFeatured");
        return Uni.createFrom()
                .completionStage(() -> arangoDatabaseAsync.collection(COLLECTION_FEATURE).insertDocument(
                        new ArangoDBProfilePictureIdentifier(userProfilePictureIdentifier),
                        new DocumentCreateOptions()
                                .waitForSync(true)
                                .overwriteMode(OverwriteMode.update)))
                .map((response) -> userProfilePictureIdentifier)
                .onFailure(ArangoDBException.class)
                .transform(exception -> {
                    LOG.error(exception);
                    openTelemetryTracingService.markSpanInError(span);
                    return new UserProfilPictureFeaturedRepositoryException();
                })
                .onTermination()
                .invoke(() -> openTelemetryTracingService.endSpan(span));
    }

    public void onStart(@Observes final StartupEvent ev) {
        if (!arangoDB.db(dbName).exists()) {
            arangoDB.createDatabase(dbName);
        }
        final ArangoDatabase arangoDatabase = arangoDB.db(dbName);
        if (!arangoDatabase.collection(ArangodbUserProfilPictureFeaturedRepository.COLLECTION_FEATURE).exists()) {
            arangoDatabase.createCollection(ArangodbUserProfilPictureFeaturedRepository.COLLECTION_FEATURE);
        }
        arangoDatabaseAsync = arangoDBAsync.db(dbName);
    }

}
