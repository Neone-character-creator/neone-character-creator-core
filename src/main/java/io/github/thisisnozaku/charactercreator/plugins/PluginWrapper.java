package io.github.thisisnozaku.charactercreator.plugins;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import io.github.thisisnozaku.charactercreator.data.access.FileAccessor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Damie on 1/27/2017.
 */
public class PluginWrapper {
    private final PluginDescription pluginDescription;
    private final GamePlugin plugin;
    private final PluginManager pluginManager;
    private final FileAccessor fileAccessor;
    private final Map<String,String> resourceMappings;

    public PluginWrapper(PluginDescription pluginDescription,
                         GamePlugin plugin,
                         PluginManager pluginManager,
                         FileAccessor fileAccessor,
                         Map<String, String> resourceMappings) {
        this.pluginDescription = pluginDescription;
        this.plugin = plugin;
        this.pluginManager = pluginManager;
        this.fileAccessor = fileAccessor;
        this.resourceMappings = resourceMappings;
    }

    public PluginDescription getPluginDescription() {
        return pluginDescription;
    }

    public GamePlugin getPlugin() {
        return plugin;
    }

    public Map<String, String> getResourceMappings() {
        return resourceMappings;
    }

    public Optional<InputStream> getResourceAsStream(String resourceName){
        try {
            return fileAccessor.getUrlContent(pluginManager.getPluginResource(pluginDescription, resourceName).toURL());
        } catch (MalformedURLException ex){
            throw new RuntimeException(ex);
        }
    }
}
