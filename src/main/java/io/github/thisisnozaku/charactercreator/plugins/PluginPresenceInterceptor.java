package io.github.thisisnozaku.charactercreator.plugins;

import io.github.thisisnozaku.charactercreator.controllers.GamePagesController;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Damien on 1/23/2016.
 */
@Component
public class PluginPresenceInterceptor extends HandlerInterceptorAdapter {
    private PluginManager pluginManager;

    @Inject
    public PluginPresenceInterceptor(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Map<String, String> templateParameters = (Map<String, String>) request.getAttribute("org.springframework.web.servlet.HandlerMapping.uriTemplateVariables");
        if (!templateParameters.isEmpty()) {
            String author = URLDecoder.decode(templateParameters.get("author"), "UTF-8");
            String game = URLDecoder.decode(templateParameters.get("game"), "UTF-8");
            String version = URLDecoder.decode(templateParameters.get("version"), "UTF-8");
            Optional<GamePlugin> plugin = pluginManager.getPlugin(author, game, version);
            if (!plugin.isPresent()) {
                throw new MissingPluginException();
            }
        }
        return true;
    }
}
