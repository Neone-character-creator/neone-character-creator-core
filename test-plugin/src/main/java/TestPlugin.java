import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Created by Damien on 1/4/2016.
 */
@Component
@Service
public class TestPlugin implements GamePlugin {
    @Override
    public PluginDescription getPluginDescription() {
        return new PluginDescription("Damien Marble", "Test", "1.0");
    }

    private final String characterViewName = "character.html";
    private final String descriptionViewName = "description.html";

    @Override
    public Character getNewCharacter() {
        return null;
    }

    @Override
    public String getCharacterViewResourceName() {
        return characterViewName;
    }

    @Override
    public String getDescriptionViewResourceName() {
        return descriptionViewName;
    }
}
