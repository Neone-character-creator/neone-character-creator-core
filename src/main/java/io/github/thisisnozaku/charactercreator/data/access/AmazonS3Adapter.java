package io.github.thisisnozaku.charactercreator.data.access;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
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
        ObjectMetadata metadata = null;
        return new S3BackedFileInformation(path);
    }

    @Override
    public List<FileInformation> getAllFileInformation(String path) {
        List<FileInformation> objects = new LinkedList<>();
        s3.listObjects(bucket, path).getObjectSummaries().forEach(s3ObjectSummary -> {
            if (!Paths.get(s3ObjectSummary.getKey()).equals(Paths.get(path))) {
                GetObjectRequest get = new GetObjectRequest(bucket, s3ObjectSummary.getKey());
                objects.add(
                        new FileInformation(s3.getUrl(bucket, s3ObjectSummary.getKey()),
                                s3ObjectSummary.getLastModified().toInstant()));
            }
        });
        return objects;
    }

    @Override
    public Optional<InputStream> getUrlContent(URL url) {
        //Strip leading slash, s3 key doesn't expect it
        String path = url.getPath().substring(1);
        if (s3.doesObjectExist(bucket, path)) {
            GetObjectRequest request = new GetObjectRequest(bucket, path);
            return Optional.of(s3.getObject(request).getObjectContent());
        }
        return Optional.empty();
    }

    public class S3BackedFileInformation extends FileInformation {
        private final String objectKey;
        private final Instant lastModified;

        public S3BackedFileInformation(String objectKey) {
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
            return s3.getUrl(bucket, objectKey);
        }

        @Override
        public Optional<Instant> getLastModifiedTimestamp() {
            return Optional.ofNullable(lastModified);
        }
    }
}