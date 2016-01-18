package io.github.thisisnozaku.charactercreator.plugins;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
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
        String author = URLDecoder.decode(templateParameters.get("author"), "UTF-8");
        String game = URLDecoder.decode(templateParameters.get("gamename"), "UTF-8");
        String version = URLDecoder.decode(templateParameters.get("version"), "UTF-8");
        PluginDescription incomingPluginDescription = new PluginDescription(author, game, version);
        Optional<GamePlugin> plugin = pluginManager.getPlugin(author, game, version);
        if (plugin.isPresent()) {
            Class<Character> characterClass = plugin.get().getCharacterType();
            ObjectReader reader = new ObjectMapper().readerFor(characterClass);
            byte[] requestBody = IOUtils.toByteArray(request.getInputStream());
            Character character = characterClass.newInstance();
            if (requestBody.length > 0) {
                try {
                    character = reader.withValueToUpdate(character).readValue(requestBody);
                    if (!incomingPluginDescription.equals(character.getPluginDescription())) {
                        throw new CharacterPluginMismatchException(incomingPluginDescription, character.getPluginDescription());
                    }
                } finally {
                    IOUtils.closeQuietly(request.getInputStream());
                }
            }
            return character;
        }
        throw new MissingPluginException();
    }
}
