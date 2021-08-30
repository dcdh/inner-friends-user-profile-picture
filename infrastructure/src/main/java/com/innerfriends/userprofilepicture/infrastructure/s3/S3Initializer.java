package com.innerfriends.userprofilepicture.infrastructure.s3;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Objects;

@ApplicationScoped
public class S3Initializer {

    private final S3Client s3client;
    private final String bucketUserProfilePictureName;

    public S3Initializer(final S3Client s3client,
                         @ConfigProperty(name = "bucket.user.profile.picture.name") final String bucketUserProfilePictureName) {
        this.s3client = Objects.requireNonNull(s3client);
        this.bucketUserProfilePictureName = Objects.requireNonNull(bucketUserProfilePictureName);
    }

    public void onStartup(@Observes StartupEvent startupEvent) {
        if (s3client.listBuckets().buckets().stream().map(Bucket::name).noneMatch(name -> bucketUserProfilePictureName.equals(name))) {
            s3client.createBucket(CreateBucketRequest.builder().bucket(bucketUserProfilePictureName).build());
        }
        if (!BucketVersioningStatus.ENABLED
                .equals(s3client.getBucketVersioning(GetBucketVersioningRequest
                        .builder()
                        .bucket(bucketUserProfilePictureName).build()).status())) {
            s3client.putBucketVersioning(PutBucketVersioningRequest.builder()
                    .bucket(bucketUserProfilePictureName)
                    .versioningConfiguration(
                            VersioningConfiguration.builder()
                                    .status(BucketVersioningStatus.ENABLED)
                                    .build())
                    .build());
        }

    }

}
