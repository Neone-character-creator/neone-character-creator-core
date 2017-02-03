package io.github.thisisnozaku.charactercreator.plugins.internal;

import com.jayway.jsonpath.JsonPath;
import io.github.thisisnozaku.charactercreator.data.access.FileAccessor;
import io.github.thisisnozaku.charactercreator.data.access.FileInformation;
import io.github.thisisnozaku.charactercreator.plugins.*;
import io.github.thisisnozaku.charactercreator.plugins.monitors.PluginMonitor;
import io.github.thisisnozaku.charactercreator.plugins.monitors.PluginMonitorEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateProcessingParameters;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * Created by Damien on 11/27/2015.
 */
@Service("pluginManager")
public class PluginManagerImpl implements PluginManager<PluginWrapper>, PluginThymeleafResourceResolver {
    private final Map<PluginDescription, PluginWrapper> plugins = new HashMap<PluginDescription, PluginWrapper>();
    private final Map<PluginDescription, Bundle> pluginBundles = new HashMap<>();
    private Framework framework;
    Logger logger = LoggerFactory.getLogger(PluginManagerImpl.class);
    private final ReentrantReadWriteLock bundleLock = new ReentrantReadWriteLock();
    @Value("${plugins.path}")
    private String pluginPath;
    @Inject
    private FileAccessor fileAccess;
    @Inject
    private PluginMonitor pluginMonitor;

    @PostConstruct
    private void init() {
        logger.info("Starting Plugin manager");
        try {
            ResourceBundle configResource = ResourceBundle.getBundle("config");
            Map<String, String> config = new HashMap<>();
            System.setProperty("felix.fileinstall.dir", pluginPath);
            String property = System.getProperty("felix.fileinstall.dir");
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
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
                    logger.info("Getting plugin at {} ready", serviceEvent.getServiceReference().getBundle().getLocation());
                    Bundle serviceBundle = serviceEvent.getServiceReference().getBundle();
                    //Load plugin.json.
                    URL pluginDescriptionFile = null;
                    PluginDescription pluginDescription = null;
                    Map<String, String> resourcePaths = new HashMap<>();
                    if ((pluginDescriptionFile = serviceBundle.getResource("plugin.json")) != null) {
                        try {
                            logger.info("Trying to read plugin.json");
                            String creator = JsonPath.read(pluginDescriptionFile.openStream(),
                                    "$.description.creator");
                            String game = JsonPath.read(pluginDescriptionFile.openStream(),
                                    "$.description.game");
                            String version = JsonPath.read(pluginDescriptionFile.openStream(),
                                    "$.description.version");
                            resourcePaths =
                                    JsonPath.read(pluginDescriptionFile.openStream(),
                                            "$.resources");
                            pluginDescription = new PluginDescription(creator, game, version);
                        } catch (IOException ex) {
                            logger.error(ex.getLocalizedMessage());
                        }
                    }
                    if (pluginDescriptionFile == null) {
                        logger.info("Trying to read plugin.xml");
                        pluginDescriptionFile = serviceBundle.getResource("plugin.xml");
                    }
                    if (pluginDescriptionFile == null) {
                        throw new IllegalStateException(String.format("While attempting to load the plugin at %s, no " +
                                "plugin description file was found", serviceBundle.getLocation()));
                    }
                    PluginWrapper wrapper = new PluginWrapper(pluginDescription, (GamePlugin) service, this, fileAccess, resourcePaths);
                    switch (serviceEvent.getType()) {
                        case ServiceEvent.REGISTERED:
                            logger.info("Game plugin {}-{}-{} registered.", wrapper.getPluginDescription().getAuthor(),
                                    wrapper.getPluginDescription().getVersion(),
                                    wrapper.getPluginDescription().getVersion());
                            plugins.put(wrapper.getPluginDescription(), wrapper);
                            pluginBundles.put(wrapper.getPluginDescription(), serviceEvent.getServiceReference().getBundle());
                            break;
                        case ServiceEvent.UNREGISTERING:
                            plugins.remove(wrapper.getPluginDescription());
                            pluginBundles.remove(wrapper.getPluginDescription());
                            break;
                    }
                }
            });
            framework.getBundleContext().addBundleListener(bundleEvent -> {
                switch (bundleEvent.getType()) {
                    case Bundle.STOPPING:
                        break;
                }
            });
            Consumer<PluginMonitorEvent> update = (event) -> {
                try {
                    FileInformation info = fileAccess.getFileInformation(event.getPluginUrl());
                    Bundle b = framework.getBundleContext().getBundle(info.getFileUrl().toExternalForm());
                    Optional<Instant> timestamp = info.getLastModifiedTimestamp();
                    if (timestamp.isPresent() && (b == null || timestamp.get().isAfter(Instant.ofEpochMilli(b.getLastModified())))) {
                        logger.info("A new plugin found at url {}, loading it.", info.getFileUrl().toExternalForm());
                        b = loadBundle(info.getFileUrl());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            pluginMonitor.onCreated(update).onModified(update);
            pluginMonitor.onDeleted(event -> {
                try {
                    FileInformation info = fileAccess.getFileInformation(event.getPluginUrl());
                    Bundle b = framework.getBundleContext().getBundle(info.getFileUrl().toExternalForm());
                    try {
                        if (b != null) {
                            b.uninstall();
                        }
                        Optional<Map.Entry<PluginDescription, Bundle>> entry = pluginBundles.entrySet().stream().filter(e -> {
                            return e.getValue().equals(b);
                        }).findFirst();
                        if (entry.isPresent()) {
                            pluginBundles.remove(entry.get().getKey());
                            plugins.remove(entry.get().getKey());
                        }
                    } catch (BundleException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            framework.start();
            pluginMonitor.start();
            logger.info("Looking for plugins in \"{}\"", pluginPath);
            //Initial attempt to load all bundles.
            Collection<FileInformation> fileInfo = fileAccess.getAllFileInformation(pluginPath);
            logger.info("Found {} plugins in \"{}\".", fileInfo.size(), pluginPath);
            fileInfo.forEach(p -> {
                loadBundle(p.getFileUrl());
            });
        } catch (BundleException ex) {
            logger.error(ex.getLocalizedMessage());
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
    public Optional<PluginWrapper> getPlugin(String author, String game, String version) {
        PluginDescription description = new PluginDescription(author, game, version);
        return Optional.ofNullable(plugins.get(description));
    }

    @Override
    public Collection<PluginDescription> getAllPluginDescriptions() {
        return plugins.keySet();
    }

    @Override
    public Optional<PluginWrapper> getPlugin(PluginDescription pluginDescription) {
        return Optional.ofNullable(plugins.get(pluginDescription));
    }

    @Override
    public URI getPluginResource(PluginDescription pluginDescription, String s) {
        try {
            return fileAccess.getFileInformation(s).getFileUrl().toURI();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Bundle loadBundle(URL path) {
        try {
            Optional<InputStream> in = fileAccess.getUrlContent(path);
            if (in.isPresent()) {
                InputStream inStream = in.get();
                Bundle bundle = framework.getBundleContext().getBundle(path.toExternalForm());
                if (bundle != null) {
                    bundle.update(inStream);
                } else {
                    bundle = framework.getBundleContext().installBundle(new File(path.toURI()).getName(), inStream);
                    bundle.start();
                }

                return bundle;
            } else {
                logger.debug("Tried to get stream for {} but it wasn't found.", path.toString());
                return null;
            }
        } catch (BundleException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            resource = plugins.get(pluginDescription).getResourceMappings().get(pluginNameTokens[3]);
            resourceUrl = getPluginResource(pluginDescription, resource).toURL();
            if (resourceUrl == null) {
                throw new IOException("Stream for resource " + resourceName + " was null.");
            }
            return resourceUrl.openStream();
        } catch (IOException ex) {
            logger.error(String.format("Tried to get an input stream from plugin %s for resource %s but an exception occurred: %s", pluginDescription.toString(), resourceName, ex.getMessage()));
        } finally {
        }
        return null;
    }

    private void uninstallBundle(Bundle b) {

    }
}
