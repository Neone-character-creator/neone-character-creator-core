package io.github.thisisnozaku.charactercreator.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.thisisnozaku.charactercreator.*;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestParameterPropertyValues;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.handler.Handler;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Damien on 1/9/2016.
 */
@Component
public class CharacterResolver implements HandlerMethodArgumentResolver {
    @Inject
    private PluginManager pluginManager;
    @Inject
    private HttpMessageConverters converters;

    @Inject
    public CharacterResolver(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Character.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Map<String, String> templateParameters = (Map<String, String>) request.getAttribute("org.springframework.web.servlet.HandlerMapping.uriTemplateVariables");
        ObjectMapper mapper = new ObjectMapper();
        String author = URLDecoder.decode(templateParameters.get("author"), "UTF-8");
        String game = URLDecoder.decode(templateParameters.get("gamename"), "UTF-8");
        String version = URLDecoder.decode(templateParameters.get("version"), "UTF-8");
        Optional<GamePlugin> plugin = pluginManager.getPlugin(author, game, version);
        if (plugin.isPresent()){
            return mapper.readValue(request.getInputStream(), plugin.get().getNewCharacter().getClass());
        }
        throw new MissingPluginException();
    }
}
