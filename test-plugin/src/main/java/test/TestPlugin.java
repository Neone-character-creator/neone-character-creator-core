package test;

import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Service(value = GamePlugin.class)
@Component
public class TestPlugin extends GamePlugin {

}
