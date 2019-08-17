package io.github.thisisnozaku.charactercreator.data.access;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Damie on 3/7/2017.
 */
public class FileAccessorTest {
    /**
     * Get file information from string path.
     *
     * @throws Exception
     */
    @Test
    public void getFileInformation() throws Exception {
        FileAccessor fileAccessor = new LocalFileSystemAccess();
        File tmp = File.createTempFile("tmp", ".txt");
        assertNotNull(fileAccessor.getFileInformation(tmp.toURI().toURL().toExternalForm()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void getFileInformationNonExistentFile() throws Exception {
        FileAccessor fileAccessor = new LocalFileSystemAccess();
        File tmp = File.createTempFile("tmp", ".txt");
        tmp.delete();
        FileInformation fileInformation = fileAccessor.getFileInformation(tmp.toURI().toURL().toExternalForm());
        assertTrue(!fileInformation.getLastModifiedTimestamp().isPresent());
    }

    @Test(expected = MalformedURLException.class)
    public void getFileInformationMalformedPath() throws IOException, URISyntaxException {
        FileAccessor fileAccessor = new LocalFileSystemAccess();
        assertNull(fileAccessor.getFileInformation("bad url"));
    }

    @Test
    public void getFileInformationFromUrl() throws IOException, URISyntaxException {
        FileAccessor fileAccessor = new LocalFileSystemAccess();
        File tmp = File.createTempFile("tmp", ".txt");
        assertNotNull(fileAccessor.getFileInformation(tmp.toURI().toURL()));
    }

    @Test
    public void getFileInformationFromNullUrl() throws IOException, URISyntaxException {
        FileAccessor fileAccessor = new LocalFileSystemAccess();
        URL url = null;
        try {
            assertNotNull(fileAccessor.getFileInformation(url));
        } catch (RuntimeException ex){
            assertTrue(ex.getCause().getClass() == MalformedURLException.class);
        }
    }

    @Test
    public void getFileInformationFromNonFileUrl() throws IOException, URISyntaxException {
        FileAccessor fileAccessor = new LocalFileSystemAccess();
        URL url = new URL("http://google.com");
        FileInformation fi = fileAccessor.getFileInformation(url);
        assertNotNull(fi);
        assertTrue(!fi.getLastModifiedTimestamp().isPresent());
    }

    @Test
    public void getAllFileInformationDirectory() throws Exception {
        FileAccessor fileAccessor = new LocalFileSystemAccess();
        Path tmpDir = Files.createTempDirectory("tmp");
        Path tmp1 = Files.createTempFile(tmpDir, "", "");
        Path tmp2 = Files.createTempFile(tmpDir, "", "");
        List<FileInformation> fileInformation = fileAccessor.getAllFileInformation(tmpDir.toUri().getRawPath());
        assertTrue(fileInformation.stream().filter(fi -> {
            try {
                return fi.getFileUrl().equals(tmp1.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).findAny().isPresent());
        assertTrue(fileInformation.stream().filter(fi -> {
            try {
                return fi.getFileUrl().equals(tmp2.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).findAny().isPresent());
    }

    @Test
    public void getContent() throws Exception {

    }

}