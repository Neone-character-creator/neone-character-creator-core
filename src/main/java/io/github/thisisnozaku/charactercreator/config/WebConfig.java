package io.github.thisisnozaku.charactercreator.config;

import io.github.thisisnozaku.charactercreator.mail.EmailSender;
import io.github.thisisnozaku.charactercreator.mail.AppMailSender;
import io.github.thisisnozaku.charactercreator.plugins.CharacterResolver;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import io.github.thisisnozaku.charactercreator.plugins.PluginResourceResolver;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.inject.Inject;
import java.util.List;
import java.util.Properties;

/**
 * Created by Damien on 1/8/2016.
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter{
    @Inject
    private PluginManager pluginManager;
    @Inject
    private PluginResourceResolver pluginResourceResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new CharacterResolver(pluginManager));
    }

    @Bean
    public SimpleMappingExceptionResolver exceptionResolver() {
        SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
        Properties properties = new Properties();
        properties.setProperty("MissingPluginException", "missing-plugin");

        resolver.setExceptionMappings(properties);
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("**/pluginresource/**").setCacheControl(CacheControl.noStore())
                .resourceChain(true).addResolver(pluginResourceResolver);

        super.addResourceHandlers(registry);
    }
}
