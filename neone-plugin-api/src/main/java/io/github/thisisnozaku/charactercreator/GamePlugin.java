package io.github.thisisnozaku.charactercreator;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * Interface for a NEOne game system plugin.
 * <p>
 * Created by Damien on 11/22/2015.
 */
public abstract class GamePlugin<T extends Character> extends AbstractModuleDescriptor<T> {

    public GamePlugin(ModuleFactory moduleFactory) {
        super(moduleFactory);
    }

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
    public abstract <T extends Character> T getNewCharacter();
}
