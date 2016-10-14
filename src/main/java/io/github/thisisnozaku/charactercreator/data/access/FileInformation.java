package io.github.thisisnozaku.charactercreator.data.access;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.time.Instant;

/**
 * Class describing information for a given file accessible by a FileAccessor implementation.
 *
 * Contains the URL for the file and a timestamp of when it was last modified.
 */
public class FileInformation {
    private URL fileUrl;
    private Instant lastModifiedTimestamp;

    public FileInformation(@NotNull URL fileUrl, Instant lastModifiedTimestamp) {
        this.fileUrl = fileUrl;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public URL getFileUrl() {
        return fileUrl;
    }

    public Instant getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }
}
