package io.github.thisisnozaku.charactercreator.plugins.monitors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Monitors directories on the local file system for changes and executes callbacks upon seeing changes in the watched
 * directories.
 * Created by Damien on 12/1/2016.
 */
@Profile("dev")
@Service
public class FileSystemMonitor extends PluginMonitorAdapter {
    private final WatchService watchService;
    private long pollTime;
    private ScheduledFuture poll;
    @Inject
    private ScheduledExecutorService executor;
    private Runnable pollingMethod;

    public FileSystemMonitor(ScheduledExecutorService executor, long pollingTime, Collection<String> directories) throws IOException {
        pollTime = pollingTime;
        watchService = FileSystems.getDefault().newWatchService();
        Collection<WatchKey> watchKeys = new LinkedList<>();
        directories.stream().forEach(d -> {
            Path path = Paths.get(d);
            try {
                if (path.toFile().exists()) {
                    watchKeys.add(path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        this.executor = executor;
        pollingMethod = () -> {
            watchKeys.stream().forEach(watchKey -> {
                Path parentPath = (Path) watchKey.watchable();
                List<WatchEvent<?>> events = watchKey.pollEvents();
                if (events.size() > 0) events.stream().forEach(watchEvent -> {
                    try {
                        Collection<Consumer<PluginMonitorEvent>> callables = null;
                        PluginMonitorEvent pluginMonitorEvent = null;
                        if (watchEvent.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                            pluginMonitorEvent = new PluginMonitorEvent(EventType.CREATED, parentPath.resolve(watchEvent.context().toString()).toFile().toURI().toURL().toExternalForm());
                        } else if (watchEvent.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                            pluginMonitorEvent = new PluginMonitorEvent(EventType.DELETED, parentPath.resolve(watchEvent.context().toString()).toFile().toURI().toURL().toExternalForm());
                        } else if (watchEvent.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                            pluginMonitorEvent = new PluginMonitorEvent(EventType.MODIFIED, parentPath.resolve(watchEvent.context().toString()).toFile().toURI().toURL().toExternalForm());
                        }
                        final PluginMonitorEvent pluginEvent = pluginMonitorEvent;
                        handle(pluginMonitorEvent);
                        if (callables != null && callables.size() > 0) {
                            callables.forEach(c -> {
                                c.accept(pluginEvent);
                            });
                        }
                    } catch (MalformedURLException ex) {
                        throw new IllegalStateException(ex);
                    }
                });
            });
        };
    }

    public FileSystemMonitor(ScheduledExecutorService executor, long pollingTime, String... directories) throws
            IOException {
        this(executor, pollingTime, Arrays.asList(directories));
    }

    public long getPollTime() {
        return pollTime;
    }

    public void setPollTime(long pollTime) {
        this.pollTime = pollTime;
    }

    public void cancelPolling(boolean immediately) {
        poll.cancel(immediately);
    }

    public void resumePolling() {
        if (poll.isCancelled()) {
            poll = executor.scheduleAtFixedRate(pollingMethod, 0, this.pollTime, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isPolling() {
        return !(poll.isCancelled() || poll.isDone());
    }

    @Bean
    public static FileSystemMonitor fileSystemMonitor(ScheduledExecutorService executorService) throws IOException {
        return new FileSystemMonitor(executorService, 1000L, "plugins");
    }

    @Bean
    public static ScheduledExecutorService scheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        poll = executor.scheduleAtFixedRate(pollingMethod, 0, pollTime, TimeUnit.MILLISECONDS);
    }
}