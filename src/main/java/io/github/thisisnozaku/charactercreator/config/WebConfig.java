package io.github.thisisnozaku.charactercreator.config;

import io.github.thisisnozaku.charactercreator.plugins.PluginResourceResolver;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by Damien on 1/8/2016.
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Inject
    private PluginResourceResolver pluginResourceResolver;
    @Inject
    private RequestMappingHandlerAdapter handlerAdapter;

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
                .resourceChain(false).addResolver(pluginResourceResolver);
    }

    @Bean
    static public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        return configurer;
    }

    /**
     * http://stackoverflow.com/a/19847526/1503554
     */
    @PostConstruct
    private void prioritizeCustomArgumentHandlers() {
        List<HandlerMethodArgumentResolver> argumentResolvers =
                new ArrayList<>(handlerAdapter.getArgumentResolvers());
        List<HandlerMethodArgumentResolver> customResolvers =
                handlerAdapter.getCustomArgumentResolvers();
        argumentResolvers.removeAll(customResolvers);
        argumentResolvers.addAll(0, customResolvers);
        handlerAdapter.setArgumentResolvers(argumentResolvers.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @Bean
    public static ObjectMapper defaultObjectMapper(){
        return new ObjectMapper();
    }

}
