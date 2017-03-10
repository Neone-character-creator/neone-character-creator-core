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
    private Instant lastModifiedTimestamp;

    public FileInformation(@NotNull URL fileUrl,
                           Instant lastModifiedTimestamp) {
        this.fileUrl = fileUrl;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
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
        return Optional.ofNullable(lastModifiedTimestamp);
    }
}
