package io.github.thisisnozaku.charactercreator.plugins;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by Damien on 11/22/2015.
 */
public interface Character {
    BigInteger getId();

    void setId(BigInteger id);

    /**
     * Returns the PluginDescription for the GamePlugin implementation that this Character is associated with.
     *
     * @return  the PluginDescription for the associated plugin.
     */
    PluginDescription getPluginDescription();

    void setPluginDescription(PluginDescription pluginDescription);
}
