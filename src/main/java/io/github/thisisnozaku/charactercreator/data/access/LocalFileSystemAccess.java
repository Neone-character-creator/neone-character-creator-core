package io.github.thisisnozaku.charactercreator.data.access;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Damien on 9/11/2016.
 */
@Profile("dev")
@Service
public class LocalFileSystemAccess implements FileAccessor {
    @Override
    public FileInformation getUrl(String path) {
        try {
            File file = new File(path);
            return new FileInformation(file.toURI().toURL(), Instant.ofEpochMilli(file.lastModified()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<FileInformation> getUrls(String path) {
        List<FileInformation> urls = new LinkedList<>();
        Path p = Paths.get(path);
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
                stream.forEach(filePath -> {
                    try {
                        urls.add(new FileInformation(filePath.toUri().toURL(), Instant.ofEpochMilli(Files.readAttributes(filePath, BasicFileAttributes.class).creationTime().toMillis())));
                    } catch (IOException ex){
                        ex.printStackTrace();
                    }
                });
            }
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
        return urls;
    }

    @Override
    public InputStream getUrlContent(URL path) {
        try {
            return path.openStream();
        }catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }
}
