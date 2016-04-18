package io.github.thisisnozaku.charactercreator.plugins;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Interface for a NEOne game system plugin.
 * <p>
 * Created by Damien on 11/22/2015.
 */
@Component(componentAbstract = true)
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
    public Class<T> getCharacterType();

    /**
     * Return the name of the resource for the character view for this plugin.
     * @return
     */
    public String getCharacterViewResourceName();

    /**
     * Return the name of the resource for the description view for this plugin.
     *
     * @return
     */
    public String getDescriptionViewResourceName();

}
