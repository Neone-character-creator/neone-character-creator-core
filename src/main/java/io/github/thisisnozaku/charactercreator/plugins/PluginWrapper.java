package io.github.thisisnozaku.charactercreator.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Wraps a plugin bundle.
 *
 * Created by Damien on 1/27/2017.
 */
public class PluginWrapper {
    private final PluginDescription pluginDescription;
    private final GamePlugin plugin;
    private final PluginManager<GamePlugin<Character>> pluginManager;
    private final Map<String, String> resourceMappings;

    public PluginWrapper(PluginDescription pluginDescription,
                         GamePlugin plugin,
                         PluginManager pluginManager,
                         Map<String, String> resourceMappings) {
        this.pluginDescription = pluginDescription;
        this.plugin = plugin;
        this.pluginManager = pluginManager;
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

    public Optional<InputStream> getResourceAsStream(String resourceName) {
        Optional<URI> uri = pluginManager.getPluginResource(pluginDescription, resourceName);
        if(uri.isPresent()){
            try {
                return Optional.of(uri.get().toURL().openStream());
            } catch (IOException ex){
                ex.printStackTrace();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
