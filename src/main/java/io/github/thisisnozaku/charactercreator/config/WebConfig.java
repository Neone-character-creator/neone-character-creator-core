package io.github.thisisnozaku.charactercreator.config;

import io.github.thisisnozaku.charactercreator.plugins.CharacterResolver;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import io.github.thisisnozaku.charactercreator.plugins.PluginPresenceInterceptor;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Damien on 1/8/2016.
 */
@Configuration
public class WebConfig extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter {
    @Inject
    private PluginManager pluginManager;
    @Inject
    private PluginPresenceInterceptor pluginPresenceInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new CharacterResolver(pluginManager));
    }

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry){
        interceptorRegistry.addInterceptor(pluginPresenceInterceptor);
    }
}
