package io.github.thisisnozaku.charactercreator.data.access;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of FileAccessor backed by an Amazon S3 bucket.
 * Created by Damien on 9/11/2016.
 */
@Service
@Profile("aws")
public class AmazonS3Adapter implements FileAccessor {
    private String bucket;
    @Inject
    private AmazonS3 s3;

    //FIXME: Allows only a single bucket to be used in the whole application. Make into a list?
    public AmazonS3Adapter(AmazonS3 s3Client, @Value("${amazon.s3.bucket}")String s3Bucket) {
        s3 = s3Client;
        bucket = s3Bucket;
    }

    @Override
    public FileInformation getFileInformation(String path) {
        return new S3BackedFileInformation(path);
    }

    @Override
    public FileInformation getFileInformation(URL path) {
        return getFileInformation(path.toExternalForm());
    }

    @Override
    public List<FileInformation> getAllFileInformation(String path) {
        List<FileInformation> objects = new LinkedList<>();
        s3.listObjects(bucket, path).getObjectSummaries().forEach(s3ObjectSummary -> {
            //Ignore the root of the pseudo-directory we're iterating through.
            if (!Paths.get(s3ObjectSummary.getKey()).equals(Paths.get(path))) {
                GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(bucket, s3ObjectSummary.getKey());
                objects.add(new FileInformation(s3.getUrl(bucket, s3ObjectSummary.getKey())));
            }
        });
        return objects;
    }

    @Override
    public <T extends FileInformation> Optional<InputStream> getContent(T file) {
        if (file == null) {
            return Optional.empty();
        }
        if (!S3BackedFileInformation.class.isInstance(file)) {
            throw new IllegalArgumentException("FileInformation is not for an S3 object.");
        }
        S3BackedFileInformation s3FileInformation = (S3BackedFileInformation) file;
        try {
            return Optional.ofNullable(s3FileInformation.getFileUrl().openStream());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public class S3BackedFileInformation extends FileInformation {
        private final String objectKey;

        public S3BackedFileInformation(String resourcePath) {
            //Strip leading /
            super(s3.getUrl(AmazonS3Adapter.this.bucket,
                    resourcePath = (resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath)
                            .substring(resourcePath.indexOf("amazonaws.com/") + "amazonaws.com/".length())));
            this.objectKey = resourcePath;
            s3.getObjectMetadata(bucket, resourcePath).getLastModified().toInstant();
        }

        public String getObjectKey() {
            return objectKey;
        }

        @Override
        public URL getFileUrl() {
            return s3.generatePresignedUrl(bucket, objectKey, Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)));
        }

        @Override
        public Optional<Instant> getLastModifiedTimestamp() {
            return Optional.ofNullable(s3.getObject(bucket, objectKey).getObjectMetadata().getLastModified().toInstant());
        }
    }

    public String getBucket() {
        return bucket;
    }
}