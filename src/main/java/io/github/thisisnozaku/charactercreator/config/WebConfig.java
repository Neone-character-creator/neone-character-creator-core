package io.github.thisisnozaku.charactercreator.config;

import io.github.thisisnozaku.charactercreator.plugins.CharacterResolver;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Damien on 1/8/2016.
 */
@Configuration
public class WebConfig extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {

    @Inject
    PluginManager pluginManager;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new CharacterResolver(pluginManager));
    }
}
