package io.github.thisisnozaku.charactercreator.plugins;


import java.net.URI;
import java.util.Collection;
import java.util.Optional;

/**
 * Service interface for managing GamePlugins
 * Created by Damien on 11/29/2015.
 */
public interface PluginManager{
    Optional<GamePlugin> getPlugin(String author, String game, String version);

    Collection<PluginDescription> getAllPluginDescriptions();

    Optional<GamePlugin> getPlugin(PluginDescription pluginDescription);

    URI getPluginResource(PluginDescription incomingPluginDescription, String resourceName);
}
