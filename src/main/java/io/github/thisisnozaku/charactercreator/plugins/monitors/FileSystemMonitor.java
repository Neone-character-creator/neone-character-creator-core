package io.github.thisisnozaku.charactercreator.plugins.monitors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Monitors directories on the local file system for changes and executes callbacks upon seeing changes in the watched
 * directories.
 * Created by Damien on 12/1/2016.
 */
@Profile("dev")
@Service
public class FileSystemMonitor extends PluginMonitorAdapter{
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
                List<WatchEvent<?>> events = watchKey.pollEvents();
                if (events.size() > 0) events.stream().forEach(watchEvent -> {
                    PluginMonitorEvent pluginMonitorEvent = null;
                    Collection<Consumer<PluginMonitorEvent>> callables = null;
                    if (watchEvent.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        callables = (Collection<Consumer<PluginMonitorEvent>>)callbacks.get(EventType.CREATED);
                        pluginMonitorEvent = new PluginMonitorEvent(EventType.CREATED, watchEvent.context().toString());
                    } else if (watchEvent.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                        callables = (Collection<Consumer<PluginMonitorEvent>>)callbacks.get(EventType.DELETED);
                        pluginMonitorEvent = new PluginMonitorEvent(EventType.DELETED, watchEvent.context().toString());
                    } else if (watchEvent.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                        callables = (Collection<Consumer<PluginMonitorEvent>>)callbacks.get(EventType.MODIFIED);
                        pluginMonitorEvent = new PluginMonitorEvent(EventType.MODIFIED, watchEvent.context().toString());
                    }
                    final PluginMonitorEvent pluginEvent = pluginMonitorEvent;
                    if (callables != null && callables.size() > 0) {
                        callables.forEach(c -> {
                            c.accept(pluginEvent);
                        });
                    }
                });
            });
        };
        poll = executor.scheduleAtFixedRate(pollingMethod, 0, pollingTime, TimeUnit.MILLISECONDS);
    }

    public FileSystemMonitor(ScheduledExecutorService executor, long pollingTime, String... directories) throws IOException {
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
    public static ScheduledExecutorService scheduledExecutorService(){
        return Executors.newSingleThreadScheduledExecutor();
    }
}