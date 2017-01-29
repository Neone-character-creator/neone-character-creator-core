package io.github.thisisnozaku.charactercreator.plugins.monitors;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Created by Damien on 12/5/2016.
 */
public class PluginMonitorEvent {
    private final PluginMonitor.EventType eventType;
    private final String pluginUrl;

    public PluginMonitorEvent(PluginMonitor.EventType eventType, String pluginUrl) {
        this.eventType = eventType;
        this.pluginUrl = pluginUrl;
    }

    public PluginMonitor.EventType getEventType() {
        return eventType;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }
}