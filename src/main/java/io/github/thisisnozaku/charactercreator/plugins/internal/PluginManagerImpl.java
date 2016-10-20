package io.github.thisisnozaku.charactercreator.plugins.internal;

import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.github.thisisnozaku.charactercreator.data.access.FileAccessor;
import io.github.thisisnozaku.charactercreator.data.access.FileInformation;
import io.github.thisisnozaku.charactercreator.plugins.*;
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
import org.yaml.snakeyaml.util.UriEncoder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Damien on 11/27/2015.
 */
@Service("pluginManager")
public class PluginManagerImpl implements PluginManager, PluginThymeleafResourceResolver {
    private final Map<PluginDescription, PluginWrapper> plugins = new HashMap<PluginDescription, PluginWrapper>();
    private final Map<PluginDescription, Bundle> pluginBundles = new HashMap<>();
    private Framework framework;
    Logger logger = LoggerFactory.getLogger(PluginManagerImpl.class);
    private final ReentrantReadWriteLock bundleLock = new ReentrantReadWriteLock();
    @Value("${plugins.directory}")
    private String pluginDirectory;
    @Inject
    private FileAccessor fileAccess;
    @Value("${plugins.pollingWait}")
    private int pollingWait;

    @PostConstruct
    private void init() {
        logger.info("Starting Plugin manager");
        try {
            ResourceBundle configResource = ResourceBundle.getBundle("config");
            Map<String, String> config = new HashMap<>();
            System.setProperty("felix.fileinstall.dir", pluginDirectory);
            String property = System.getProperty("felix.fileinstall.dir");
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
                    Bundle serviceBundle = serviceEvent.getServiceReference().getBundle();
                    //Load plugin.json.
                    URL pluginDescriptionFile = null;
                    PluginDescription pluginDescription = null;
                    Map<String, String> resourcePaths = new HashMap<>();
                    if((pluginDescriptionFile= serviceBundle.getResource("plugin.json")) != null){
                        try {
                            String creator = JsonPath.read(fileAccess.getUrlContent(pluginDescriptionFile),
                                    "$.description.creator");
                            String game = JsonPath.read(fileAccess.getUrlContent(pluginDescriptionFile),
                                    "$.description.game");
                            String version = JsonPath.read(fileAccess.getUrlContent(pluginDescriptionFile),
                                    "$.description.version");
                            resourcePaths = new ObjectMapper().readValue(
                                    JsonPath.<String>read(fileAccess.getUrlContent(pluginDescriptionFile),
                                            "$.description.resources"), HashMap.class
                            );
                            pluginDescription = new PluginDescription(creator, game, version);
                        } catch (IOException ex){
                            logger.error(ex.getLocalizedMessage());
                        }
                    }
                    if(pluginDescriptionFile== null) {
                        pluginDescriptionFile = serviceBundle.getResource("plugin.xml");
                    }
                    if(pluginDescriptionFile == null){
                        throw new IllegalStateException(String.format("While attempting to load the plugin at %s, no " +
                                "plugin description file was found", serviceBundle.getLocation()));
                    }
                    PluginWrapper wrapper = new PluginWrapper(pluginDescription, (GamePlugin) service, this, resourcePaths);
                    plugins.put(pluginDescription, wrapper);
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
            framework.start();
            new Thread() {
                public void run() {
                    try {
                        while (true) {
                            List<FileInformation> bundleInformation = fileAccess.getUrls("plugins");
                            try {
                                List<Bundle> presentBundles = new LinkedList<>();
                                for (FileInformation info : bundleInformation) {
                                    Bundle b = framework.getBundleContext().getBundle(info.getFileUrl().toExternalForm());
                                    if (b == null || info.getLastModifiedTimestamp().isAfter(Instant.ofEpochMilli(b.getLastModified()))) {
                                        logger.info("A new plugin found at url {}, loading it.", info.getFileUrl().toExternalForm());
                                        b = loadBundle(info.getFileUrl());
                                   }
                                    presentBundles.add(b);
                                }
                                Iterator<Map.Entry<PluginDescription, Bundle>> i = pluginBundles.entrySet().iterator();
                                while (i.hasNext()) {
                                    Map.Entry<PluginDescription, Bundle> next = i.next();
                                    if (!presentBundles.contains(next.getValue())) {
                                        next.getValue().uninstall();
                                        plugins.remove(next.getKey());
                                        i.remove();
                                    }
                                }
                            } catch (BundleException ex){
                                ex.printStackTrace();
                            } finally {
                                Thread.sleep(pollingWait);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
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
    public Optional<PluginWrapper> getPlugin(String author, String game, String version) {
        return Optional.ofNullable(plugins.get(new PluginDescription(author, game, version)));
    }

    @Override
    public Collection<PluginDescription> getAllPluginDescriptions() {
        Collection<PluginDescription> returnVal = plugins.keySet();
        return returnVal;
    }

    @Override
    public Optional<PluginWrapper> getPlugin(PluginDescription pluginDescription) {
        return Optional.ofNullable(plugins.get(pluginDescription));
    }

    @Override
    public InputStream getPluginResourceAsStream(PluginDescription incomingPluginDescription, String resourceName) {
        URL resourceUrl = null;
        try {
            resourceUrl = plugins.get(incomingPluginDescription).getResourceUrl(resourceName);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (NullPointerException ex) {
            return null;
        } finally {
        }
        return fileAccess.getUrlContent(resourceUrl);
    }

    private Bundle loadBundle(URL path) {
        try {
            InputStream in = fileAccess.getUrlContent(path);
            Bundle bundle = framework.getBundleContext().getBundle(path.toExternalForm());
            if (bundle != null) {
                bundle.uninstall();
            }
            bundle = framework.getBundleContext().installBundle(path.toExternalForm(), in);
            bundle.start();
            return bundle;
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
        try {
            URL resourceUrl = null;
            String resource = null;
            resource = plugins.get(pluginDescription).getResourcePath(pluginNameTokens[3]);
            resourceUrl = getPluginResourceURL(pluginDescription, resource);
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

    @Override
    public URL getPluginResourceURL(PluginDescription pluginDescription, String name) {
        Bundle bundle = pluginBundles.get(pluginDescription);
        URL resourceUrl = bundle.getResource(name);
        return resourceUrl;
    }
}
