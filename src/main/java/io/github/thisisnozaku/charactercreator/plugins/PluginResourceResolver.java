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
import java.net.URI;
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
            requestPath = URLDecoder.decode(requestPath, "UTF-8");
            String[] pathTokens = requestPath.substring(requestPath.indexOf("games/") + "games/".length()).split("/");
            String author = pathTokens[0];
            String game = pathTokens[1];
            String version = pathTokens[2];
            PluginDescription incomingPluginDescription = new PluginDescription(URLDecoder.decode(author, "UTF-8"),
                    URLDecoder.decode(game, "UTF-8"),
                    URLDecoder.decode(version, "UTF-8"));
            if(requestPath.contains("pluginresource")){
                requestPath = requestPath.substring(requestPath.indexOf("pluginresource/") + "pluginresource/".length());
            }
            URI resourceUri = pluginManager.getPluginResource(incomingPluginDescription, requestPath);
            return resourceUri != null ? new UrlResource(resourceUri) : null;
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
        return resourcePath;
    }
}
