package io.github.thisisnozaku.charactercreator.plugins;

/**
 * Created by Damien on 11/22/2015.
 */
public interface Character {
    Long getId();

    void setId(Long id);

    /**
     * Returns the PluginDescription for the GamePlugin implementation that this Character is associated with.
     *
     * @return  the PluginDescription for the associated plugin.
     */
    PluginDescription getPlugin();
}
