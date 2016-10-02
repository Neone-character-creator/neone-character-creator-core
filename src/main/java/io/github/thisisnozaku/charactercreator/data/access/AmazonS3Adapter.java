package io.github.thisisnozaku.charactercreator.data.access;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Damien on 9/11/2016.
 */
@Service
@Profile("aws")
public class AmazonS3Adapter implements FileAccess {
    @Value("${amazon.s3.bucket}")
    private String bucket;
    private AmazonS3Client s3;

    public AmazonS3Adapter() {
        AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        ClientConfiguration config = new ClientConfiguration();
        s3 = new AmazonS3Client(credentials, config);
    }

    @Override
    public FileInformation getUrl(String path) {
        return new FileInformation(s3.getUrl(bucket, path),
                s3.getObjectMetadata(bucket, path).getLastModified().toInstant());
    }

    public List<FileInformation> getUrls(String path) {
        List<FileInformation> urls = new LinkedList<>();
        s3.listObjects(bucket, path).getObjectSummaries().forEach(s3ObjectSummary -> {
            if (!Paths.get(s3ObjectSummary.getKey()).equals(Paths.get(path))) {
                urls.add(
                        new FileInformation(s3.getUrl(bucket, s3ObjectSummary.getKey()),
                                s3ObjectSummary.getLastModified().toInstant()));
            }
        });
        return urls;
    }

    @Override
    public InputStream getUrlContent(URL url) {
        String bucket = url.getHost().substring(0, url.getHost().indexOf(".s3"));
        //Strip leading slash, s3 key doesn't expect it
        String path = url.getPath().substring(1);
        GetObjectRequest request = new GetObjectRequest(bucket, path);

        return s3.getObject(request).getObjectContent();
    }

}