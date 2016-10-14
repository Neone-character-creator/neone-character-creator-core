package io.github.thisisnozaku.charactercreator.data.access;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by Damien on 9/11/2016.
 */
public interface FileAccessor {
    /**
     * Get URL to file at the given path, for either a directory or file.
     * @param path
     * @return
     */
    FileInformation getUrl(String path);

    /**
     * Get all child URLs at the given path if it is a directory. Returns null if path resolves to a file
     * @param path
     * @return
     */
    List<FileInformation> getUrls(String path);

    /**
     * Get an input stream for the resource at the given URL. The method is primarily for implementations that
     * have special access requirements, such as for security reasons.
     * @param path
     * @return
     */
    InputStream getUrlContent(URL path);
}
