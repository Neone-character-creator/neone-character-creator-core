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
 * Created by Damien on 9/11/2016.
 */
@Service
@Profile("aws")
public class AmazonS3Adapter implements FileAccessor {
    @Value("${amazon.s3.bucket}")
    private String bucket;
    @Inject
    private AmazonS3 s3;

    public AmazonS3Adapter(AmazonS3 s3Client) {
        s3 = s3Client;
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
            if (!Paths.get(s3ObjectSummary.getKey()).equals(Paths.get(path))) {
                GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(bucket, s3ObjectSummary.getKey());
                objects.add(
                        new FileInformation(s3.getUrl(bucket, s3ObjectSummary.getKey()),
                                s3ObjectSummary.getLastModified().toInstant()));
            }
        });
        return objects;
    }

    @Override
    public <T extends FileInformation> Optional<InputStream> getContent(T file) {
        if(file == null){
            return Optional.empty();
        }
        if(!S3BackedFileInformation.class.isInstance(file)){
            throw new IllegalArgumentException("FileInformation is not for an S3 object.");
        }
        S3BackedFileInformation s3FileInformation = (S3BackedFileInformation) file;
        try {
            return Optional.ofNullable(s3FileInformation.getFileUrl().openStream());
        }catch (IOException ex){
            throw new IllegalStateException(ex);
        }
    }

    public class S3BackedFileInformation extends FileInformation {
        private final String objectKey;
        private final Instant lastModified;

        public S3BackedFileInformation(String objectKey) {
            //Strip leading /
            objectKey = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
            if (s3.doesObjectExist(bucket, objectKey)) {
                lastModified = s3.getObjectMetadata(bucket, objectKey).getLastModified().toInstant();
            } else {
                lastModified = null;
            }
            this.objectKey = objectKey;
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
            return Optional.ofNullable(lastModified);
        }
    }
}