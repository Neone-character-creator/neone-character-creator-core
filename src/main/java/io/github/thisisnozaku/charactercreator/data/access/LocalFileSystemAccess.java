package io.github.thisisnozaku.charactercreator.data.access;

import org.apache.felix.scr.annotations.Service;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Damien on 9/11/2016.
 */
@Profile("dev")
@Service
public class LocalFileSystemAccess implements FileAccess {
    @Override
    public URL getUrl(String path) {
        try {
            return Paths.get(path).toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<URL> getUrls(String path) {
        List<URL> urls = new LinkedList<>();
        Path p = Paths.get(path);
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
                stream.forEach(filePath -> {
                    try {
                        urls.add(filePath.toUri().toURL());
                    } catch (MalformedURLException ex){
                        ex.printStackTrace();
                    }
                });
            }
        } catch (IOException ex){
            ex.printStackTrace();
            return null;
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
