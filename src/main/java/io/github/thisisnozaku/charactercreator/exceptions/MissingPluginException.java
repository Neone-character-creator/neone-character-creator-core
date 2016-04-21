package io.github.thisisnozaku.charactercreator.exceptions;

import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;

/**
 * Exception generated when trying to access a plugin that is not available.
 * Created by Damien on 12/7/2015.
 */
public class MissingPluginException extends RuntimeException {
    private final PluginDescription missingPlugin;
    public MissingPluginException(PluginDescription missingPlugin){
        this.missingPlugin = missingPlugin;
    }

    public PluginDescription getMissingPlugin() {
        return missingPlugin;
    }
}
