package io.github.thisisnozaku.charactercreator.controllers.plugins;

import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/plugins")
public class PluginsController {
    private PluginManager<? extends GamePlugin<? extends Character>> pluginManager;

    @Inject
    public PluginsController(PluginManager<? extends GamePlugin<? extends Character>> pluginManager) {
        this.pluginManager = pluginManager;
    }

    @RequestMapping("")
    @CrossOrigin
    public Collection<PluginDescription> getPresentPlugins() {
        return pluginManager.getAllPluginDescriptions();
    }

    @RequestMapping(value = "/{author}/{system}/{version}/", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> getPluginPackage(@PathVariable("author") String author, @PathVariable("system") String system, @PathVariable("version") String version) {
        PluginDescription pluginDescription = new PluginDescription(author, system, version);
        Optional<InputStream> contentStream = pluginManager.getPluginArchive(pluginDescription)
                .map(archiveUri -> {
                    try {
                        return archiveUri.toURL().openStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        return contentStream
                .map(cs -> ResponseEntity.ok(new InputStreamResource(cs))).orElse(ResponseEntity.notFound().build());
    }
}
