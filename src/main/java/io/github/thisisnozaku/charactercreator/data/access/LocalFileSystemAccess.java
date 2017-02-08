package io.github.thisisnozaku.charactercreator.data.access;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of FileAccessor for use with a local file system.
 * Created by Damien on 9/11/2016.
 */
@Profile("dev")
@Service
public class LocalFileSystemAccess implements FileAccessor {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LocalFileSystemAccess.class);

    @Override
    public FileInformation getFileInformation(String path) {
        try {
            URL url = new URL(path);
            File f = null;
            try {
                f = new File(url.toURI());
            } catch (IllegalArgumentException ex) {
                logger.warn("Path {} is an invalid file URL. Reason: {} Timestamp information is unavailable.", url.toExternalForm(), ex.getMessage());
            }
            Long timestamp = f != null ? f.lastModified() : Instant.now().toEpochMilli();
            return new FileInformation(url, Instant.ofEpochMilli(Math.max(timestamp, Instant.now().toEpochMilli())));
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FileInformation getFileInformation(URL path) {
        return getFileInformation(path != null ? path.toExternalForm() : null);
    }

    @Override
    public List<FileInformation> getAllFileInformation(String path) {
        List<FileInformation> urls = new LinkedList<>();
        Path p = Paths.get(path);
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
                stream.forEach(filePath -> {
                    try {
                        urls.add(new FileInformation(filePath.toUri().toURL(), Instant.ofEpochMilli(Files.readAttributes(filePath, BasicFileAttributes.class).creationTime().toMillis())));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return urls;
    }

    @Override
    public Optional<InputStream> getContent(FileInformation path) {
        try {
            return Optional.of(path.getFileUrl().openStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
