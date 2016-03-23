package io.github.thisisnozaku.charactercreator.plugins.internal;

import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import io.github.thisisnozaku.charactercreator.plugins.PluginThymeleafResourceResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateProcessingParameters;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Damien on 11/27/2015.
 */
@Service
public class PluginManagerImpl implements PluginManager, PluginThymeleafResourceResolver {
    private final Map<PluginDescription, GamePlugin> plugins = new HashMap<PluginDescription, GamePlugin>();
    private final Map<PluginDescription, Bundle> pluginBundles = new HashMap<>();
    private Framework framework;
    Logger logger = LoggerFactory.getLogger(PluginManagerImpl.class);

    @PostConstruct
    private void init() {
        try {
            ResourceBundle configResource = ResourceBundle.getBundle("config");
            Map<String, String> config = new HashMap<>();
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
            config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "io.github.thisisnozaku.charactercreator.plugins; version=1.0");
            for (String key : configResource.keySet()) {
                config.put(key, configResource.getString(key));
            }
            FrameworkFactory fmwkFactory = new org.apache.felix.framework.FrameworkFactory();
            framework = fmwkFactory.newFramework(config);
            framework.init();
            framework.getBundleContext().addServiceListener(serviceEvent -> {
                Object service = framework.getBundleContext().getService(serviceEvent.getServiceReference());
                if (service instanceof GamePlugin) {
                    GamePlugin plugin = (GamePlugin) service;
                    switch (serviceEvent.getType()) {
                        case ServiceEvent.REGISTERED:
                            plugins.put(plugin.getPluginDescription(), plugin);
                            pluginBundles.put(plugin.getPluginDescription(), serviceEvent.getServiceReference().getBundle());
                            break;
                        case ServiceEvent.UNREGISTERING:
                            plugins.remove(plugin.getPluginDescription());
                            pluginBundles.remove(plugin.getPluginDescription());
                            break;
                    }
                }
            });
            framework.start();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("plugins"))) {
                for (Path path : stream) {
                    loadBundle(path);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (BundleException ex) {
            ex.printStackTrace();
        }
    }

    @PreDestroy
    private void destroy() {
        try {
            framework.stop();
            framework.waitForStop(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BundleException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<GamePlugin> getPlugin(String author, String game, String version) {
        return Optional.ofNullable(plugins.get(new PluginDescription(author, game, version)));
    }

    @Override
    public Collection<PluginDescription> getAllPluginDescriptions() {
        return plugins.keySet();
    }

    @Override
    public Optional<GamePlugin> getPlugin(PluginDescription pluginDescription) {
        return Optional.ofNullable(plugins.get(pluginDescription));
    }

    @Override
    public URI getPluginResource(PluginDescription incomingPluginDescription, String resourceName) {
        try {
            return getBundleResourceUrl(incomingPluginDescription, resourceName).toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private Bundle loadBundle(Path path) {
        try {
            Bundle bundle = framework.getBundleContext().installBundle(path.toUri().toURL().toExternalForm());
            bundle.start();
            return bundle;
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (BundleException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @Override
    public String getName() {
        return "plugin-manager";
    }

    @Override
    public InputStream getResourceAsStream(TemplateProcessingParameters templateProcessingParameters, String resourceName) {
        String[] pluginNameTokens = resourceName.split("-");
        PluginDescription pluginDescription = new PluginDescription(pluginNameTokens[0], pluginNameTokens[1], pluginNameTokens[2]);
        Bundle bundle = pluginBundles.get(pluginDescription);
        try {
            URL resourceUrl = null;
            String resource = null;
            switch (pluginNameTokens[3]) {
                case "description":
                    resource = plugins.get(pluginDescription).getDescriptionViewResourceName();
                    break;
                case "character":
                    resource = plugins.get(pluginDescription).getCharacterViewResourceName();
                    break;
            }
            resourceUrl = getBundleResourceUrl(pluginDescription, resource);
            if (resourceUrl == null) {
                throw new IOException("Stream for resource " + resourceName + " was null.");
            }
            return resourceUrl.openStream();
        } catch (IOException ex) {
            logger.error(String.format("Tried to get an input stream from plugin %s for resource %s but an exception occured: %s", pluginDescription.toString(), resourceName, ex.getMessage()));
        }
        return null;
    }

    private URL getBundleResourceUrl(PluginDescription pluginDescription, String name) {
        Bundle bundle = pluginBundles.get(pluginDescription);
        URL resourceUrl = bundle.getResource(name);
        return resourceUrl;
    }
}
