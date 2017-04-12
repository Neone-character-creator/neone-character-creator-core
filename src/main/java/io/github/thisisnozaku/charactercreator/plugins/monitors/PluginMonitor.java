package io.github.thisisnozaku.charactercreator.plugins.monitors;

import java.awt.*;
import java.nio.file.WatchEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * This class is responsible for watching for changes among the available plugins.
 */
public interface PluginMonitor {
    /**
     * Registers a callback that is executed when the monitor detects that a file has been deleted.
     *
     * @param consumer
     * @return
     */
    PluginMonitor onDeleted(Consumer<PluginMonitorEvent> consumer);

    /**
     * Registers a callback that is executed when the monitor detects that a new file is present.
     *
     * @return
     */
    PluginMonitor onCreated(Consumer<PluginMonitorEvent> consumer);

    /**
     * Registers a callback that is executed when the monitor detects that a file has been modified.
     *
     * @return
     */
    PluginMonitor onModified(Consumer<PluginMonitorEvent> runnable);

    /**
     * Removes the given consumer from the consumers registered for the given event type.
     */
    void removeConsumer(Consumer consumer, EventType type);

    /**
     * Handle the given event, triggering any consumers.
     *
     * @param event the plugin event
     */
    void handle(PluginMonitorEvent event);

    enum EventType {
        CREATED, MODIFIED, DELETED
    }
}