package io.github.thisisnozaku.charactercreator.config;

import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

/**
 * Created by Damien on 11/15/2015.
 */
@Configuration
public class MongoConfig {
    @Inject
    private PluginManager pluginManager;
}
