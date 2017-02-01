package io.github.thisisnozaku.charactercreator.data.access;

import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Created by Damien on 9/11/2016.
 */
public interface FileAccessor {
    /**
     * Get URL to file at the given path, for either a directory or file.
     * @param path
     * @return
     */
    FileInformation getFileInformation(String path);

    /**
     * Get all child URLs at the given path if it is a directory.
     * @param path
     * @return
     */
    List<FileInformation> getAllFileInformation(String path);

    /**
     * Get an input stream for the resource at the given URL. The method is primarily for implementations that
     * have special access requirements, such as for security reasons.
     * @param path
     * @return
     */
    Optional<InputStream> getUrlContent(URL path);
}
