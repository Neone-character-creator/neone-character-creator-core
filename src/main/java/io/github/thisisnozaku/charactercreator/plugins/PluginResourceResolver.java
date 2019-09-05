package io.github.thisisnozaku.charactercreator.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resolves resources that are found in plugins.
 */
@Service
public class PluginResourceResolver implements ResourceResolver {
    private static final Logger logger = LoggerFactory.getLogger(PluginResourceResolver.class);
    @SuppressWarnings({"CanBeFinal", "unused"})
    @Inject
    private PluginManager pluginManager;

    @Override
    public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
        try {
            logger.info("Initial path {}", requestPath);
            requestPath = URLDecoder.decode(requestPath, "UTF-8");
            logger.info("URL Decoded path {}", requestPath);
            String[] pathTokens = requestPath.split("/");
            String author = pathTokens[1];
            String game = pathTokens[2];
            String version = pathTokens[3];
            logger.info("Plugin properties - Author: {} - Game: {} - Version: {}", author, game, version);
            String resourcePath = Arrays.asList(pathTokens).subList(4, pathTokens.length).stream().collect(Collectors.joining("/"));
            logger.info("Resource path {}", resourcePath);
            PluginDescription incomingPluginDescription = new PluginDescription(author, game, version);
            //If no resource is named, we get the character sheet
            if (resourcePath.equals("")) {
                resourcePath = "character";
            }
            Optional<URI> resource = pluginManager.getPluginResource(incomingPluginDescription, resourcePath);

            try {
                if (resource.isPresent()) {
                    logger.debug("Resource was found");
                    return new UrlResource(resource.get());
                } else {
                    logger.debug("Resource was NOT found");
                    return null;
                }

            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
        return resourcePath;
    }
}