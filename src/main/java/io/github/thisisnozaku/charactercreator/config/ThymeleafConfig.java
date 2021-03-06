package io.github.thisisnozaku.charactercreator.config;

import io.github.thisisnozaku.charactercreator.plugins.PluginThymeleafResourceResolver;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.StandardTemplateModeHandlers;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration class for Thymeleaf.
 *
 * Created by Damien on 1/3/2016.
 */
@Configuration
public class ThymeleafConfig{
    @Inject
    private TemplateEngine engine;
    @Inject
    private PluginThymeleafResourceResolver pluginResourceLoader;

    @PostConstruct
    public void extension(){
        TemplateResolver resolver = new TemplateResolver();
        resolver.setPrefix("");
        resolver.setSuffix("");
        Set<String> resolvablePatterns = new HashSet<>();
        //Support resolving special plugin files.
        resolvablePatterns.add("*-*-*-description");
        resolvablePatterns.add("*-*-*-character");
        resolver.setResolvablePatterns(resolvablePatterns);
        resolver.setTemplateMode(StandardTemplateModeHandlers.LEGACYHTML5.getTemplateModeName());
        resolver.setOrder(1);
        //Because plugins are dynamic, avoid caching.
        resolver.setCacheable(false);
        resolver.setResourceResolver(pluginResourceLoader);

        engine.addTemplateResolver(resolver);
    }
}
