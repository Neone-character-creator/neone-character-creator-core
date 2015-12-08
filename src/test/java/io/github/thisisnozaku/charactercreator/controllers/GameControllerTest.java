package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.Character;
import io.github.thisisnozaku.charactercreator.*;
import io.github.thisisnozaku.charactercreator.data.AccountRepository;
import io.github.thisisnozaku.charactercreator.data.CharacterDao;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Map;
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
@ContextConfiguration(classes = {NeoneCoreApplication.class})
public class GameControllerTest {
    private GameController controller;

    @Mock
    private CharacterDao characters;
    @Mock
    private AccountRepository accounts;
    @Mock
    private PluginManager plugins;
    @Mock
    private HandlerMethodArgumentResolver resolver;

    private MockMvc mvc;


    @Mock
    private GamePlugin plugin;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        controller = new GameController(characters, accounts, plugins);

        mvc = MockMvcBuilders.standaloneSetup(controller).setCustomArgumentResolvers(resolver).build();
        PluginDescription desc = new PluginDescription("Damien Marble", "Game System", "1.1");
        when(plugin.getPluginDescription()).thenReturn(desc);
        when(plugin.getNewCharacter()).thenReturn(new MockCharacter());

        when(plugins.getPlugin(isA(String.class), isA(String.class), isA(String.class))).thenAnswer((invocation -> {
            Object[] args = invocation.getArguments();
            if (args[0].equals(plugin.getPluginDescription().getAuthorName()) && args[1].equals(plugin.getPluginDescription().getSystemName()) && args[2].equals(plugin.getPluginDescription().getVersion())) {
                return Optional.of(plugin);
            } else {
                return Optional.empty();
            }
        }));

        when(resolver.supportsParameter(any(MethodParameter.class))).thenAnswer(invocation -> {
            MethodParameter param = invocation.getArgumentAt(0, MethodParameter.class);
            return param.getParameterType().equals(Character.class);
        });
        when(resolver.resolveArgument(any(MethodParameter.class), any(ModelAndViewContainer.class), any(NativeWebRequest.class), any(WebDataBinderFactory.class)))
                .thenAnswer(invocation -> {
                    Map<String, String> templateVariables = (Map<String, String>) invocation.getArgumentAt(2, NativeWebRequest.class).getNativeRequest(HttpServletRequest.class).getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                    if (invocation.getArgumentAt(0, MethodParameter.class).getParameterType().equals(Character.class) &&
                            templateVariables.get("gamename").equals(URLEncoder.encode(desc.getSystemName(), "UTF-8")) &&
                            templateVariables.get("author").equals(URLEncoder.encode(desc.getAuthorName(), "UTF-8")) &&
                            templateVariables.get("version").equals(URLEncoder.encode(desc.getVersion(), "UTF-8"))) {
                        return invocation.getArgumentAt(2, NativeWebRequest.class).getAttribute("character", RequestAttributes.SCOPE_REQUEST);
                    } else {
                        PluginDescription expected = new PluginDescription(templateVariables.get("author"),
                                templateVariables.get("gamename"),
                                templateVariables.get("version")
                        );
                        throw new CharacterPluginMismatchException(expected, null);
                    }
                });
    }

    /**
     * Test returning the plugin information page.
     *
     * @throws Exception
     */
    @Test
    public void testGetPluginInformationPage() throws Exception {
        PluginDescription desc = plugin.getPluginDescription();
        RequestBuilder request = get("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8"))
                .contentType(MediaType.TEXT_HTML);
        MvcResult result = mvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(String.format("%s-%s-%s-description", URLEncoder.encode(desc.getAuthorName(), "UTF-8"), URLEncoder.encode(desc.getSystemName(), "UTF-8"), URLEncoder.encode(desc.getVersion(), "UTF-8"))))
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
        RequestBuilder request = get("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8"))
                .contentType(MediaType.TEXT_HTML);
        mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("missing-plugin"));
    }

    /**
     * Test creating a new character and displaying the character sheet.
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        PluginDescription desc = plugin.getPluginDescription();
        RequestBuilder request = post("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/")
                .contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andDo(print())
                .andExpect(view().name(
                        URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "-" +
                                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "-" +
                                URLEncoder.encode(desc.getVersion(), "UTF-8") + "-" +
                                "character"
                ))
                .andExpect(model().attribute("character", new MockCharacter()));
        verify(characters).createCharacter(new MockCharacter());
    }

    /**
     * Test attempting to create a character for a plugin that isn't present
     *
     * @throws Exception
     */
    @Test
    public void testCreateForMissingPlugin() throws Exception {
        RequestBuilder request = post("/" +
                URLEncoder.encode("Fake", "UTF-8") + "/" +
                URLEncoder.encode("Fake", "UTF-8") + "/" +
                URLEncoder.encode("Fake", "UTF-8") + "/")
                .contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andDo(print())
                .andExpect(view().name(
                        "missing-plugin"
                ));
    }

    /**
     * Test getting a character.
     *
     * @throws Exception
     */
    @Test
    public void testGetCharacter() throws Exception {
        MockCharacter existingCharacter = new MockCharacter();
        existingCharacter.setId(1);
        when(characters.getCharacter(existingCharacter.getId(), existingCharacter.getClass())).thenAnswer(invocation -> Optional.of(existingCharacter));

        PluginDescription desc = plugin.getPluginDescription();
        RequestBuilder request = get("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                existingCharacter.getId())
                .contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andDo(print())
                .andExpect(view().name(
                        URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "-" +
                                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "-" +
                                URLEncoder.encode(desc.getVersion(), "UTF-8") + "-" +
                                "character"
                ))
                .andExpect(model().attribute("character", existingCharacter));

        verify(characters).getCharacter(existingCharacter.getId(), MockCharacter.class);
    }

    /**
     * Test getting a character that doesn't exist
     *
     * @throws Exception
     */
    @Test
    public void testGetMissingCharacter() throws Exception {
        MockCharacter existingCharacter = new MockCharacter();
        existingCharacter.setId(1);
        when(characters.getCharacter(existingCharacter.getId(), existingCharacter.getClass())).thenAnswer(invocation -> Optional.empty());

        PluginDescription desc = plugin.getPluginDescription();
        RequestBuilder request = get("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                existingCharacter.getId())
                .contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andDo(print())
                .andExpect(view().name(
                        "missing-character"
                ));
    }

    /**
     * Test getting a character via the incorrect plugin.
     */
    @Test
    public void testGettingCharacterWrongPlugin() throws Exception {
        Character wrongCharacter = new Character() {
            long id = 2;

            @Override
            public long getId() {
                return id;
            }

            @Override
            public void setId(long id) {
                this.id = id;
            }
        };
        PluginDescription desc = plugin.getPluginDescription();

        when(characters.getCharacter(wrongCharacter.getId(), MockCharacter.class)).thenThrow(ClassCastException.class);

        RequestBuilder request = get("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                wrongCharacter.getId())
                .contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andExpect(view().name(
                        "plugin-mismatch"
                ));
    }

    @Test
    public void testSaveCharacter() throws Exception {
        Character mockCharacter = new MockCharacter();
        mockCharacter.setId(1);
        PluginDescription desc = plugin.getPluginDescription();
        RequestBuilder request = put("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                mockCharacter.getId())
                .requestAttr("character", mockCharacter);

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(characters).updateCharacter(mockCharacter);
    }

    @Test
    public void testSaveCharacterWrongPlugin() throws Exception {
        Character mockCharacter = new MockCharacter();
        mockCharacter.setId(1);
        PluginDescription desc = new PluginDescription("Fake", "Fake", "Fake");
        RequestBuilder request = put("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                mockCharacter.getId());

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void testDeleteCharacter() throws Exception {
        Character mockCharacter = new MockCharacter();
        mockCharacter.setId(1);
        PluginDescription desc = plugin.getPluginDescription();
        RequestBuilder request = delete("/" +
                URLEncoder.encode(desc.getAuthorName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystemName(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                mockCharacter.getId());

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(characters).deleteCharacter(mockCharacter.getId());
    }

    private static class MockCharacter implements Character {
        private Long id;

        @Override
        public long getId() {
            return id;
        }

        @Override
        public void setId(long id) {
            if (this.id != null) {
                throw new IllegalStateException("Attempted to reassign the id of the character");
            }
            this.id = id;
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
}