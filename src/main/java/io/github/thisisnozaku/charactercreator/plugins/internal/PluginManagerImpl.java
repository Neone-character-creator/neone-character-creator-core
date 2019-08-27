package io.github.thisisnozaku.charactercreator.plugins.internal;

import com.google.common.io.Files;
import com.jayway.jsonpath.JsonPath;
import io.github.thisisnozaku.charactercreator.data.access.FileAccessor;
import io.github.thisisnozaku.charactercreator.data.access.FileInformation;
import io.github.thisisnozaku.charactercreator.plugins.*;
import io.github.thisisnozaku.charactercreator.plugins.Character;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/**
 * Implementation of PluginManager.
 * Created by Damien on 11/27/2015.
 */
@Service("pluginManager")
class PluginManagerImpl implements PluginManager<GamePlugin<Character>, Character>, PluginThymeleafResourceResolver {
    private final Map<PluginDescription, PluginWrapper> plugins = new HashMap<>();
    private final Map<PluginDescription, Bundle> pluginBundles = new HashMap<>();
    private Framework framework;
    private final Logger logger = LoggerFactory.getLogger(PluginManagerImpl.class);
    @SuppressWarnings({"CanBeFinal", "unused"})
    @Value("${plugins.path}")
    private String pluginPath;
    @SuppressWarnings({"CanBeFinal", "unused"})
    @Inject
    private FileAccessor fileAccess;
    @SuppressWarnings({"CanBeFinal", "unused"})
    @Inject
    private PluginMonitor pluginMonitor;

    @SuppressWarnings("unused")
    @PostConstruct
    private void init() {
        logger.info("Starting Plugin manager");
        logger.info("Using {} as file accessor", fileAccess.getClass().getName());
        try {
            ResourceBundle configResource = ResourceBundle.getBundle("config");
            Map<String, String> config = new HashMap<>();
            config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
            config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "io.github.thisisnozaku.charactercreator.plugins; version=1.0");
            config.put(Constants.FRAMEWORK_STORAGE, Files.createTempDir().getAbsolutePath());
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
                    URL pluginDescriptionFile;
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
                    PluginWrapper wrapper = new PluginWrapper(pluginDescription, (GamePlugin) service, this, resourcePaths);
                    switch (serviceEvent.getType()) {
                        case ServiceEvent.REGISTERED:
                            logger.info("Game plugin {}-{}-{} registered.", wrapper.getPluginDescription().getAuthor(),
                                    wrapper.getPluginDescription().getSystem(),
                                    wrapper.getPluginDescription().getVersion());
                            plugins.put(wrapper.getPluginDescription(), wrapper);
                            pluginBundles.put(wrapper.getPluginDescription(), serviceEvent.getServiceReference().getBundle());
                            break;
                        case ServiceEvent.UNREGISTERING:
                            logger.info("Game plugin {}-{}-{} removed.", wrapper.getPluginDescription().getAuthor(),
                                    wrapper.getPluginDescription().getSystem(),
                                    wrapper.getPluginDescription().getVersion());
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
                        loadBundle(info.getFileUrl());
                    } else {
                        logger.info("Previous plugin found at url {}, skipping loading", info.getFileUrl().toExternalForm());
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
                         Optional<Map.Entry<PluginDescription, Bundle>> entry = pluginBundles.entrySet().stream().filter(e -> e.getValue().equals(b)).findFirst();
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
            logger.info("Looking for plugins in \"{}\"", pluginPath);
            //Initial attempt to load all bundles.
            Collection<FileInformation> fileInfo = fileAccess.getAllFileInformation(pluginPath);
            logger.info("Found {} plugins in \"{}\".", fileInfo.size(), pluginPath);
            fileInfo.forEach(p -> {
                logger.info("Loading {}", p.getFileUrl());
                try {
                    loadBundle(new URL(p.getFileUrl().toExternalForm()));
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            });
        } catch (BundleException ex) {
            logger.error(ex.getLocalizedMessage());
        }
    }

    @SuppressWarnings("unused")
    @PreDestroy
    private void destroy() {
        try {
            framework.stop();
            framework.waitForStop(0);
        } catch (InterruptedException | BundleException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<GamePlugin<Character>> getPlugin(String author, String game, String version) {
        return getPlugin(new PluginDescription(author, game, version));
    }

    @Override
    public Collection<PluginDescription> getAllPluginDescriptions() {
        return plugins.keySet();
    }

    @Override
    public Optional<GamePlugin<Character>> getPlugin(PluginDescription pluginDescription) {
        return Optional.ofNullable(plugins.get(pluginDescription)).map(w -> w.getPlugin());
    }

    @Override
    public Optional<URI> getPluginResource(PluginDescription pluginDescription, String s) {
        try {
            //Find the bundle for the plugin.
            Bundle pluginBundle = pluginBundles.get(pluginDescription);
            Map<String, String> internalResourceMappings = plugins.get(pluginDescription).getResourceMappings();
            if (internalResourceMappings.containsKey(s)) {
                s = internalResourceMappings.get(s);
            }
            URL resourceURL = pluginBundle.getEntry(s);
            if (resourceURL == null) {
                logger.warn("No url for path {}.", s);
                return Optional.empty();
            }
            //Extract the resource from the bundle
            return Optional.of(pluginBundle.getResource(s).toURI());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Optional<Bundle> loadBundle(URL path) {
        try {
            FileInformation info = fileAccess.getFileInformation(path);
            Optional<InputStream> in = fileAccess.getContent(info);
            if (in.isPresent()) {
                InputStream inStream = in.get();
                Bundle bundle = framework.getBundleContext().getBundle(info.getFileUrl().toExternalForm());
                if (bundle != null) {
                    logger.info("Bundle already exists, updating");
                    bundle.update(inStream);
                } else {
                    logger.info("New bundle, installing");
                    bundle = framework.getBundleContext().installBundle(info.getFileUrl().toExternalForm(), inStream);
                    logger.info("Starting bundle");
                    bundle.start();
                }
                logger.info("Bundle loaded");
                return Optional.of(bundle);
            } else {
                logger.debug("Tried to get stream for {} but it wasn't found.", path.toString());
                return Optional.empty();
            }
        } catch (BundleException ex) {
             ex.printStackTrace();
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
        Map<String, String> pluginResourceMappings = plugins.get(pluginDescription).getResourceMappings();
        if (pluginResourceMappings.containsKey(pluginNameTokens[3])) {
            resourceName = pluginResourceMappings.get(pluginNameTokens[3]);
        }
        try {
            return bundle.getResource(resourceName).openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
