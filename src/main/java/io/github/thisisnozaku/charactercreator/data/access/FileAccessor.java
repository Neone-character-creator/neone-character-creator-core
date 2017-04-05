package io.github.thisisnozaku.charactercreator.data.access;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Interface for abstracting accessing resources.
 * <p>
 * Created by Damien on 9/11/2016.
 */
public interface FileAccessor {
    /**
     * Get FileInformation for the file at the given path, for either a directory or file.
     *
     * @param path path to the entry at the given path
     * @return  information about the entry
     */
    FileInformation getFileInformation(String path) throws MalformedURLException, URISyntaxException;

    /**
     * Get FileInformation for the file at the given path, for either a directory or file.
     *
     * @param path  URL to the entry
     * @return  information about the entry
     */
    FileInformation getFileInformation(URL path);

    /**
     * Get FileInformation for all children of the directory at the given path.
     *
     * @param path  path to the directory
     * @return  information of all direct children of the directory
     */
    List<FileInformation> getAllFileInformation(String path);

    /**
     * Get an input stream for the resource at the given URL. The method is primarily for implementations that
     * have special access requirements, such as for security reasons.
     *
     * @param path  FileInformation of file to get content of
     * @return  the content stream
     */
    <T extends FileInformation> Optional<InputStream> getContent(T path) throws IOException;
}
