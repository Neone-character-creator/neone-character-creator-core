package io.github.thisisnozaku.charactercreator.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thisisnozaku.charactercreator.NeoneCoreApplication;
import io.github.thisisnozaku.charactercreator.data.AccountRepository;
import io.github.thisisnozaku.charactercreator.data.CharacterDao;
import io.github.thisisnozaku.charactercreator.plugins.*;
import io.github.thisisnozaku.charactercreator.plugins.Character;
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
    @Mock
    private GamePlugin secondPlugin;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        controller = new GameController(characters, accounts, plugins);

        mvc = MockMvcBuilders.standaloneSetup(controller).setCustomArgumentResolvers(new CharacterResolver(plugins)).build();
        PluginDescription desc1 = new PluginDescription("Damien Marble", "Game System", "1.1");
        when(plugin.getPluginDescription()).thenReturn(desc1);
        when(plugin.getCharacterType()).thenReturn(MockCharacter.class);
        PluginDescription desc2 = new PluginDescription("Mamien Darble", "Second Game System", "1.0");
        when(secondPlugin.getPluginDescription()).thenReturn(desc2);
        when(secondPlugin.getCharacterType()).thenReturn(MockCharacter.class);

        when(plugins.getPlugin(isA(String.class), isA(String.class), isA(String.class))).thenAnswer((invocation -> {
            Object[] args = invocation.getArguments();
            if (args[0].equals(plugin.getPluginDescription().getAuthor()) && args[1].equals(plugin.getPluginDescription().getSystem()) && args[2].equals(plugin.getPluginDescription().getVersion())) {
                return Optional.of(plugin);
            } else if (args[0].equals(secondPlugin.getPluginDescription().getAuthor()) && args[1].equals(secondPlugin.getPluginDescription().getSystem()) && args[2].equals(secondPlugin.getPluginDescription().getVersion())) {
                return Optional.of(secondPlugin);
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
                    return invocation.getArgumentAt(2, NativeWebRequest.class).getAttribute("character", RequestAttributes.SCOPE_REQUEST);
                });

        when(characters.createCharacter(isA(Character.class))).thenAnswer(invocation -> {
            Character character = invocation.getArgumentAt(0, Character.class);
            character.setId(1L);
            return character;
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
        RequestBuilder request = get("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/info")
                .contentType(MediaType.TEXT_HTML);
        MvcResult result = mvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name(String.format("%s-%s-%s-description",desc.getAuthor(), desc.getSystem(), desc.getVersion())))
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
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/info")
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
        Character newCharacter = new MockCharacter(desc);
        ObjectMapper objectMapper = new ObjectMapper();
        String mappedObject = objectMapper.writeValueAsString(newCharacter);
        RequestBuilder request = post("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/")
                .content(mappedObject)
                .contentType(MediaType.APPLICATION_JSON_UTF8);
        newCharacter.setId(1L);

        mvc.perform(request)
                .andDo(print())
                .andExpect(view().name(
                        desc.getAuthor() + "-" +
                                desc.getSystem() + "-" +
                                desc.getVersion() + "-" +
                                "character"
                ))
                .andExpect(model().attribute("character", newCharacter));
        verify(characters).createCharacter(newCharacter);
    }

    /**
     * Test attempting to create a character for a plugin that isn't present
     *
     * @throws Exception
     */
   @Test
    public void testCreateForMissingPlugin() throws Exception {
       Character mockCharacter = new MockCharacter();
       ObjectMapper mapper = new ObjectMapper();
        RequestBuilder request = post("/games/" +
                URLEncoder.encode("Missing", "UTF-8") + "/" +
                URLEncoder.encode("Missing", "UTF-8") + "/" +
                URLEncoder.encode("Missing", "UTF-8") + "/")
                .content(mapper.writeValueAsString(mockCharacter))
                .contentType(MediaType.APPLICATION_JSON_UTF8);

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isNotFound())
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
        PluginDescription desc = plugin.getPluginDescription();
        MockCharacter existingCharacter = new MockCharacter(desc);
        existingCharacter.setId(1L);
        when(characters.getCharacter(existingCharacter.getId(), existingCharacter.getClass())).thenAnswer(invocation -> Optional.of(existingCharacter));


        RequestBuilder request = get("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                existingCharacter.getId())
                .contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andDo(print())
                .andExpect(view().name(
                        desc.getAuthor() + "-" +
                                desc.getSystem() + "-" +
                                desc.getVersion() + "-" +
                                "character"
                ))
                .andExpect(model().attribute("character", existingCharacter));

        verify(characters).getCharacter(existingCharacter.getId(), existingCharacter.getClass());
    }

    /**
     * Test getting a character that doesn't exist
     *
     * @throws Exception
     */
    @Test
    public void testGetMissingCharacter() throws Exception {
        PluginDescription desc = plugin.getPluginDescription();
        MockCharacter existingCharacter = new MockCharacter(desc);
        existingCharacter.setId(1L);
        when(characters.getCharacter(existingCharacter.getId(), existingCharacter.getClass())).thenAnswer(invocation -> Optional.empty());


        RequestBuilder request = get("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
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
            public Long getId() {
                return id;
            }

            @Override
            public void setId(Long id) {
                this.id = id;
            }

            @Override
            public PluginDescription getPluginDescription() {
                return new PluginDescription("","","");
            }

            @Override
            public void setPluginDescription(PluginDescription pluginDescription) {

            }
        };
        PluginDescription desc = plugin.getPluginDescription();

        when(characters.getCharacter(wrongCharacter.getId(), MockCharacter.class)).thenThrow(ClassCastException.class);

        RequestBuilder request = get("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
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
        PluginDescription desc = plugin.getPluginDescription();
        Character mockCharacter = new MockCharacter(desc);
        mockCharacter.setId(1L);
        ObjectMapper objectMapper = new ObjectMapper();

        RequestBuilder request = put("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                mockCharacter.getId())
                .content(objectMapper.writeValueAsString(mockCharacter));

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(characters).updateCharacter(mockCharacter);
    }

    @Test
    public void testSaveCharacterWrongPlugin() throws Exception {
        Character secondMockCharacter = new Character() {
            PluginDescription pluginDescription = plugin.getPluginDescription();
            @Override
            public Long getId() {
                return 0L;
            }

            @Override
            public void setId(Long l) {

            }

            @Override
            public PluginDescription getPluginDescription() {
                return pluginDescription;
            }

            @Override
            public void setPluginDescription(PluginDescription pluginDescription) {
                this.pluginDescription = pluginDescription;
            }
        };
        secondMockCharacter.setId(1L);

        PluginDescription desc = secondPlugin.getPluginDescription();
        ObjectMapper objectMapper = new ObjectMapper();
        String objectAsString = objectMapper.writeValueAsString(secondMockCharacter);
        RequestBuilder request = put("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                secondMockCharacter.getId()).content(objectMapper.writeValueAsString(secondMockCharacter));

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andExpect(view().name("plugin-mismatch"));
    }

    @Test
    public void testDeleteCharacter() throws Exception {
        PluginDescription desc = plugin.getPluginDescription();
        Character mockCharacter = new MockCharacter(desc);
        mockCharacter.setId(1L);

        RequestBuilder request = delete("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                mockCharacter.getId());

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(characters).deleteCharacter(mockCharacter.getId());
    }

    public static class MockCharacter implements Character {
        private Long id;
        private PluginDescription plugin;

        public MockCharacter(){};

        public MockCharacter(PluginDescription pluginDescription){
            this.plugin= pluginDescription;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            if (this.id != null) {
                throw new IllegalStateException("Attempted to reassign the id of the character");
            }
            this.id = id;
        }

        @Override
        public PluginDescription getPluginDescription() {
            return plugin;
        }

        @Override
        public void setPluginDescription(PluginDescription pluginDescription) {
            this.plugin = pluginDescription;
        }

        @Override
        public String toString() {
            return "MockCharacter{" +
                    "id=" + id +
                    ", plugin=" + plugin +
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
}