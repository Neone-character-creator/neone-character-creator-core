package test;

import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Created by Damien on 1/4/2016.
 */
@Component
@Service
public class TestPlugin implements GamePlugin {
    private final PluginDescription pluginDescription;

    public TestPlugin() {
        this.pluginDescription = new PluginDescription("Damien Marble", "Test", "1.0");
    }

    @Override
    public PluginDescription getPluginDescription() {
        return pluginDescription;
    }

    @Override
    public Class getCharacterType() {
        return test.Character.class;
    }

    private final String characterViewName = "character.html";
    private final String descriptionViewName = "description.html";

    @Override
    public String getCharacterViewResourceName() {
        return characterViewName;
    }

    @Override
    public String getDescriptionViewResourceName() {
        return descriptionViewName;
    }
}
