package io.github.thisisnozaku.charactercreator;

import java.io.File;

/**
 * Interface for a NEOne game system plugin.
 * <p>
 * Created by Damien on 11/22/2015.
 */
public abstract class GamePlugin<T extends Character> {

    /**
     * Returns the PluginDescription for this plugin.
     *
     * @return the PluginDescription
     */
    public abstract PluginDescription getPluginDescription();

    /**
     * Return a new instance of the Character implementation for the plugin.
     *
     * @return a new Character
     */
    public abstract T getNewCharacter();

    /**
     * Returns the file for the description page for the plugin.
     *
     * @return the description view file
     */
    public abstract File getDescriptionViewFile();

    /**
     * Returns the file for the character sheet page for the plugin.
     *
     * @return the character sheet view file
     */
    public abstract File getCharacterViewFile();
}
