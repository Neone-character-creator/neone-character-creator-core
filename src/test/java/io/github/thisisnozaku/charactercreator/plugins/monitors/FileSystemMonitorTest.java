package io.github.thisisnozaku.charactercreator.plugins.monitors;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.WatchEvent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by Damien on 12/2/2016.
 */

public class FileSystemMonitorTest {

    @After
    public void cleanup(){
        new File("test/test file").delete();
        new File("test").delete();
    }

    @Test
    public void testSingleFileCreateMonitor() throws IOException, InterruptedException {
        new File("test").mkdir();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        FileSystemMonitor monitor = new FileSystemMonitor(executorService, 10L, "test");
        monitor.onCreated((PluginMonitorEvent event)->{
            Object context = event.getPluginUrl();
            assertEquals("test file", context);
        });
        new File("test/test file").createNewFile();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
        monitor.cancelPolling(false);
    }

    @Test
    public void testSingleFileNoCallback() throws IOException, InterruptedException {
        new File("test").mkdir();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        FileSystemMonitor monitor = new FileSystemMonitor(executorService, 10L, "test");
        new File("test/test file").createNewFile();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
        monitor.cancelPolling(false);
    }

    @Test
    public void testSingleFileDeleteMonitor() throws IOException, InterruptedException {
        new File("test").mkdir();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        FileSystemMonitor monitor = new FileSystemMonitor(executorService, 10L, "test");
        monitor.onDeleted((PluginMonitorEvent event)->{
            assertEquals("test file", event.getPluginUrl());
        });
        File testFile = new File("test/test file");
        testFile.createNewFile();
        testFile.delete();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
        monitor.cancelPolling(false);
    }

    @Test
    public void testSingleFileModifyMonitor() throws IOException, InterruptedException {
        new File("test").mkdir();
        File testFile = new File("test/test file");
        testFile.createNewFile();
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        FileSystemMonitor monitor = new FileSystemMonitor(executorService, 10L, "test");
        monitor.onModified((PluginMonitorEvent event)->{
            Object context = event.getPluginUrl();
            assertEquals("test file", context);
        });
        testFile.setLastModified(System.currentTimeMillis());
        executorService.awaitTermination(3, TimeUnit.SECONDS);
        monitor.cancelPolling(false);
    }

    @Test
    public void testResumingPolling() throws IOException {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        FileSystemMonitor monitor = new FileSystemMonitor(executorService, 10L, "test");
        monitor.cancelPolling(true);
        assertFalse(monitor.isPolling());
    }
}