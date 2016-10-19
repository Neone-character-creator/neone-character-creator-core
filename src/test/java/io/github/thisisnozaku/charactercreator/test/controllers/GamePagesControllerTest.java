package io.github.thisisnozaku.charactercreator.test.controllers;

import io.github.thisisnozaku.charactercreator.TestConfiguration;
import io.github.thisisnozaku.charactercreator.controllers.games.GamePagesController;
import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepository;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestConfiguration.class})
public class GamePagesControllerTest {
    private GamePagesController controller;
    @Mock
    private CharacterMongoRepository characters;
    @Mock
    private UserRepository accounts;
    @Mock
    private PluginManager plugins;
    @Mock
    private HandlerMethodArgumentResolver resolver;

    private MockMvc mvc;

    @Mock
    private PluginWrapper firstPlugin;
    @Mock
    private PluginWrapper secondPlugin;
    PluginDescription desc1 = new PluginDescription("Damien Marble", "Game System", "1.1");
    PluginDescription desc2 = new PluginDescription("Mamien Darble", "Second Game System", "1.0");

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        controller = new GamePagesController(characters, accounts, plugins);

        mvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(plugins.getPlugin(isA(String.class), isA(String.class), isA(String.class))).thenAnswer((invocation -> {
            Object[] args = invocation.getArguments();
            if (args[0].equals(desc1.getAuthor()) && args[1].equals(desc1.getSystem()) && args[2].equals(desc1.getVersion())) {
                return Optional.of(firstPlugin);
            } else if (args[0].equals(desc2.getAuthor()) && args[1].equals(desc2.getSystem()) && args[2].equals(desc2.getVersion())) {
                return Optional.of(secondPlugin);
            } else {
                return Optional.empty();
            }
        }));

        when(plugins.getPlugin(isA(PluginDescription.class))).thenAnswer(invocation -> {
            PluginDescription pluginDescription = (PluginDescription) invocation.getArguments()[0];
            return plugins.getPlugin(pluginDescription.getAuthor(), pluginDescription.getSystem(), pluginDescription.getVersion());
        });

        when(resolver.supportsParameter(any(MethodParameter.class))).thenAnswer(invocation -> {
            MethodParameter param = invocation.getArgumentAt(0, MethodParameter.class);
            return param.getParameterType().equals(Character.class);
        });

        when(resolver.resolveArgument(any(MethodParameter.class), any(ModelAndViewContainer.class), any(NativeWebRequest.class), any(WebDataBinderFactory.class)))
                .thenAnswer(invocation -> {
                    return invocation.getArgumentAt(2, NativeWebRequest.class).getAttribute("character", RequestAttributes.SCOPE_REQUEST);
                });

        when(characters.save(isA(CharacterDataWrapper.class))).thenAnswer(invocation -> {
            CharacterDataWrapper character = invocation.getArgumentAt(0, CharacterDataWrapper.class);
            if (character.getId() == null) {
                character.setId(BigInteger.ONE.toString());
            }
            return character;
        });;
    }

    /**
     * Test returning the firstPlugin information page.
     *
     * @throws Exception
     */
    @Test
    public void testGetPluginInformationPage() throws Exception {
        PluginDescription desc = desc1;
        RequestBuilder request = get("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/pages/info")
                .contentType(MediaType.TEXT_HTML);
        MvcResult result = mvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("plugin-character-page"))
                .andReturn();
    }

    /**
     * Attempt to get the plugin description page for a plugin that isn't present.
     *
     * @throws Exception
     */
    @Test
    public void testGetInformationPageMissingPlugin() throws Exception {
        PluginDescription desc = new PluginDescription("Fake", "Fake", "Fake");
        RequestBuilder request = get("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/pages/info")
                .contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("missing-plugin"));
    }

    public static class MockCharacter extends Character {
        private BigInteger id;

        public MockCharacter() {
        }

        public MockCharacter(PluginDescription pluginDescription) {
            setPluginDescription(pluginDescription);
        }

        public BigInteger getId() {
            return id;
        }

        public void setId(BigInteger id) {
            if (this.id != null) {
                throw new IllegalStateException("Attempted to reassign the id of the character");
            }
            this.id = id;
        }

        @Override
        public String toString() {
            return "MockCharacter{" +
                    "id=" + id +
                    ", plugin=" + getPluginDescription() +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!this.getClass().isInstance(obj)) {
                return false;
            }
            MockCharacter other = (MockCharacter) obj;
            return Objects.equals(this.id, other.id);
        }
    }

    @ExceptionHandler(MissingPluginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String missingPlugin() {
        return "missing-plugin";
    }
}