package io.github.thisisnozaku.charactercreator.data.access;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Optional;

/**
 * Class describing information for a given file accessible by a FileAccessor implementation.
 * <p>
 * Contains the URL for the file and a timestamp of when it was last modified.
 */
public class FileInformation {
    private URL fileUrl;

    public FileInformation(@NotNull URL fileUrl) {
        this.fileUrl = fileUrl;
    }

    public URL getFileUrl() {
        return fileUrl;
    }

    /**
     * Returns the last modified time of the resource pointed to by this, if available.
     *
     * @return
     */
    public Optional<Instant> getLastModifiedTimestamp() {
        try {
            Instant timestamp = Instant.ofEpochMilli(Files.readAttributes(Paths.get(fileUrl.toURI()), BasicFileAttributes.class).creationTime().toMillis());
            return Optional.of(timestamp);
        } catch (URISyntaxException | IOException | FileSystemNotFoundException ex) {
            return Optional.empty();
        }
    }
}
