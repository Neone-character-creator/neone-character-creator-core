package io.github.thisisnozaku.charactercreator.exceptions;

import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception generated when attempting to perform an action, when the plugin that is required is
 * Created by Damien on 12/7/2015.
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class CharacterPluginMismatchException extends RuntimeException {
    private final PluginDescription requiredPlugin;
    private final PluginDescription actualPlugin;

    public CharacterPluginMismatchException(PluginDescription requiredPlugin, PluginDescription actualPlugin) {
        this.requiredPlugin = requiredPlugin;
        this.actualPlugin = actualPlugin;
    }

    public PluginDescription getRequiredPlugin() {
        return requiredPlugin;
    }

    public PluginDescription getActualPlugin() {
        return actualPlugin;
    }
}
