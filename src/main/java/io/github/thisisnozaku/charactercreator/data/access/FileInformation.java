package io.github.thisisnozaku.charactercreator.data.access;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;

/**
 * Class describing information for a given file accessible by a FileAccessor implementation.
 * <p>
 * Contains the URL for the file and a timestamp of when it was last modified.
 */
public class FileInformation {
    private URL fileUrl;
    private Optional<Instant> lastModifiedTimestamp;

    protected FileInformation(){}

    public FileInformation(@NotNull URL fileUrl,
                           Instant lastModifiedTimestamp) {
        this.fileUrl = fileUrl;
        this.lastModifiedTimestamp = lastModifiedTimestamp != null ? Optional.of(lastModifiedTimestamp) : Optional.empty();
    }

    public URL getFileUrl() {
        return fileUrl;
    }

    public Optional<Instant> getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }
}
