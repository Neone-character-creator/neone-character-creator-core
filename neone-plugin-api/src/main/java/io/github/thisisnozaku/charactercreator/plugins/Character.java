package io.github.thisisnozaku.charactercreator.plugins;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by Damien on 11/22/2015.
 */
public abstract class Character {
    private PluginDescription pluginDescription;

    public PluginDescription getPluginDescription() {
        return pluginDescription;
    }

    public void setPluginDescription(PluginDescription pluginDescription) {
        this.pluginDescription = pluginDescription;
    }
}
