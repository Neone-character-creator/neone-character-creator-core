package io.github.thisisnozaku.charactercreator.plugins.monitors;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Created by Damien on 12/3/2016.
 */
public class PluginMonitorAdapter implements PluginMonitor {
    protected final EnumMap<EventType, Collection<Consumer<PluginMonitorEvent>>> callbacks = new EnumMap<>(EventType.class);

    public PluginMonitorAdapter() {
        Arrays.asList(EventType.values()).stream().forEach(v -> callbacks.put(v, new LinkedList<>()));
    }

    @Override
    public PluginMonitor onDeleted(Consumer<PluginMonitorEvent> callback) {
        callbacks.get(EventType.DELETED).add(callback);
        return this;
    }

    @Override
    public PluginMonitor onCreated(Consumer<PluginMonitorEvent> callback) {
        callbacks.get(EventType.CREATED).add(callback);
        return this;
    }

    @Override
    public PluginMonitor onModified(Consumer<PluginMonitorEvent> callback) {
        callbacks.get(EventType.MODIFIED).add(callback);
        return this;
    }
}
