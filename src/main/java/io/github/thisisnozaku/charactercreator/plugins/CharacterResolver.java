package io.github.thisisnozaku.charactercreator.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Damien on 1/9/2016.
 */
@Component
public class CharacterResolver implements HandlerMethodArgumentResolver {
    private PluginManager pluginManager;

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
        String author = URLDecoder.decode(templateParameters.get("author"), "UTF-8");
        String game = URLDecoder.decode(templateParameters.get("game"), "UTF-8");
        String version = URLDecoder.decode(templateParameters.get("version"), "UTF-8");
        PluginDescription incomingPluginDescription = new PluginDescription(author, game, version);
        Optional<GamePlugin> plugin = pluginManager.getPlugin(author, game, version);
        String requestContentType = request.getContentType();
        Character character = null;
        if (requestContentType != null) {
            switch (requestContentType) {
                case MediaType.APPLICATION_JSON_UTF8_VALUE:
                case MediaType.APPLICATION_JSON_VALUE:
                    ObjectMapper objectMapper = new ObjectMapper();
                    character = objectMapper.readerFor(plugin.get().getCharacterType()).readValue(request.getInputStream());
                    break;
                case MediaType.APPLICATION_FORM_URLENCODED_VALUE:
                    BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(plugin.get().getCharacterType().newInstance());
                    HttpMessageConverter converter = new FormHttpMessageConverter();
                    MultiValueMap<String, String> values = (MultiValueMap<String, String>) converter.read(plugin.get().getCharacterType(), new ServletServerHttpRequest(request));
                    for (Map.Entry<String, List<String>> entry : values.entrySet()){
                        for(String value : entry.getValue()){
                            beanWrapper.setPropertyValue(entry.getKey(), value);
                        }
                    }
                    character = (Character) beanWrapper.getWrappedInstance();
                    break;
            }
            character.setPluginDescription(incomingPluginDescription);
            return character;
        }
        character = (Character) plugin.get().getCharacterType().newInstance();
        character.setPluginDescription(plugin.get().getPluginDescription());
        return character;
    }
}
