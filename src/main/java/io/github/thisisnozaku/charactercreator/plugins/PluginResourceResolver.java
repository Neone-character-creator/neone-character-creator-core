package io.github.thisisnozaku.charactercreator.plugins;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;
import org.thymeleaf.resourceresolver.UrlResourceResolver;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves resources that are found in plugins.
 */
@Service
public class PluginResourceResolver implements ResourceResolver {
    @Inject
    private PluginManager pluginManager;

    @Override
    public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
        try {
            String fullPath = request.getServletPath();
            String[] pathTokens = fullPath.substring(fullPath.indexOf("games/") + "games/".length()).split("/");
            String author = pathTokens[0];
            String game = pathTokens[1];
            String version = pathTokens[2];
            PluginDescription incomingPluginDescription = new PluginDescription(author, game, version);
            String resourceName = fullPath.substring(fullPath.indexOf("pluginresources/") + "pluginresources/".length());
            return new UrlResource(pluginManager.getPluginResource(incomingPluginDescription, resourceName));
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
        return resourcePath;
    }
}