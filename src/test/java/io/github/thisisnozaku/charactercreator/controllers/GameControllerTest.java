package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.NeoneCoreApplication;
import io.github.thisisnozaku.charactercreator.data.AccountRepository;
import io.github.thisisnozaku.charactercreator.data.CharacterDao;
import io.github.thisisnozaku.charactercreator.model.Character;
import io.github.thisisnozaku.charactercreator.model.GamePlugin;
import io.github.thisisnozaku.charactercreator.model.PluginDescription;
import io.github.thisisnozaku.charactercreator.model.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URLEncoder;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by Damien on 11/22/2015.
 */
@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {NeoneCoreApplication.class})
public class GameControllerTest {
    @InjectMocks
    private GameController controller;

    @Mock
    private CharacterDao characters;
    @Mock
    private AccountRepository accounts;
    @Mock
    private PluginManager plugins;

    private MockMvc mvc;

    @Mock
    private GamePlugin plugin;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        controller = new GameController(characters, accounts, plugins);

        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        PluginDescription desc = new PluginDescription("Damien Marble", "Game System", "1.1");
        when(plugin.getPluginDescription()).thenReturn(desc);
        when(plugin.getNewCharacter()).thenReturn(new MockCharacter());

        when(plugins.getPlugin(plugin.getPluginDescription().getAuthorName(), plugin.getPluginDescription().getSystemName(), plugin.getPluginDescription().getVersion())).thenReturn(Optional.of(plugin));
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
    }

    @Test
    public void testCreateForMissingPlugin() throws Exception {
        when(plugins.getPlugin("Fake", "Fake", "Fake")).thenReturn(Optional.ofNullable(null));
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

    @Test
    public void testGetCharacter() throws Exception {
        MockCharacter existingCharacter = new MockCharacter();
        existingCharacter.setId(1);
        when(characters.getCharacter(existingCharacter.getId(), existingCharacter.getClass())).thenAnswer(invocation -> {
            return Optional.of(existingCharacter);
        });

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
    }

    @Test
    public void testGetMissingCharacter() throws Exception {
        MockCharacter existingCharacter = new MockCharacter();
        existingCharacter.setId(1);
        when(characters.getCharacter(existingCharacter.getId(), existingCharacter.getClass())).thenAnswer(invocation -> {
            return Optional.ofNullable(null);
        });

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