package test;

import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.PluginEventListener;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import java.util.Optional;

/**
 * Created by Damien on 1/4/2016.
 */
@Service(value = GamePlugin.class)
@Component
public class TestPlugin extends GamePlugin {

}
