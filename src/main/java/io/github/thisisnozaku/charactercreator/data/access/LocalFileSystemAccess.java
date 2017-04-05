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
 * <p>
 * Created by Damien on 9/11/2016.
 */
@Profile("dev")
@Service
public class LocalFileSystemAccess implements FileAccessor {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LocalFileSystemAccess.class);

    @Override
    public FileInformation getFileInformation(String path) throws MalformedURLException, URISyntaxException {
        return getFileInformation(new URL(path));
    }

    @Override
    public FileInformation getFileInformation(URL path) throws URISyntaxException {
        File f = null;
        try {
            f = new File(path.toURI());
        } catch (IllegalArgumentException ex) {
            logger.warn("Path {} is an invalid file URL. Reason: {} - Timestamp information is unavailable.", path.toExternalForm(), ex.getMessage());
        }
        Long timestamp = f != null ? f.lastModified() : Instant.now().toEpochMilli();
        return new FileInformation(path);
    }

    @Override
    public List<FileInformation> getAllFileInformation(String path) {
        List<FileInformation> urls = new LinkedList<>();
        try {
            Path p = Paths.get(new File(path).toURI());
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
                stream.forEach(filePath -> {
                    try {
                        urls.add(new FileInformation(filePath.toUri().toURL()));
                    } catch (IOException ex) {
                        //Should never happen; can't throw checked exception inside a lambda
                        throw new RuntimeException(ex);
                    }
                });
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return urls;
    }

    @Override
    public Optional<InputStream> getContent(FileInformation path) throws IOException {
        if (path == null) {
            return Optional.empty();
        }
        return Optional.of(path.getFileUrl().openStream());
    }
}
