package io.github.thisisnozaku.charactercreator.plugins;

import java.io.File;

/**
 * Interface for a NEOne game system plugin.
 * <p>
 * Created by Damien on 11/22/2015.
 */
public interface GamePlugin<T extends Character> {

    /**
     * Returns the PluginDescription for this plugin.
     *
     * @return the PluginDescription
     */
    public PluginDescription getPluginDescription();

    /**
     * Return a new instance of the Character implementation for the plugin.
     *
     * @return a new Character
     */
    public T getNewCharacter();

    /**
     * Return the name of the resource for the character view for this plugin.
     * @return
     */
    public String getCharacterViewResourceName();

    /**
     * Return the name of the resource for the description view for this plugin.
     * @return
     */
    public String getDescriptionViewResourceName();

}
