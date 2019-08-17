package io.github.thisisnozaku.charactercreator.test.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thisisnozaku.charactercreator.TestConfiguration;
import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.controllers.games.GameRestController;
import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepositoryCustom;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.plugins.*;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestConfiguration.class})
@TestPropertySource("classpath:/application-dev.properties")
public class RestControllerTest {
    private GameRestController controller;
    private CharacterMongoRepositoryCustom characters = Mockito.mock(CharacterMongoRepositoryCustom.class);
    @Mock
    private PluginManager plugins;
    private MockMvc mvc;

    private PluginWrapper firstPlugin;
    private PluginWrapper secondPlugin;
    PluginDescription desc1 = new PluginDescription("Damien Marble", "Game System", "1.1");
    PluginDescription desc2 = new PluginDescription("Mamien Darble", "Second Game System", "1.0");

    @Before
    public void setup() throws Exception {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(new User(1L, Collections.emptyList()), null));
        characters = Mockito.mock(CharacterMongoRepositoryCustom.class);
        plugins = Mockito.mock(PluginManager.class);
        firstPlugin = Mockito.mock(PluginWrapper.class);
        secondPlugin = Mockito.mock(PluginWrapper.class);

        controller = new GameRestController(characters, plugins);

        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(plugins.getPlugin(isA(PluginDescription.class))).thenAnswer((invocation -> {
            PluginDescription description = (PluginDescription) invocation.getArguments()[0];
            return plugins.getPlugin(description.getAuthor(), description.getSystem(), description.getVersion());
        }));

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

        when(characters.save(isA(CharacterDataWrapper.class))).thenAnswer(invocation -> {
            CharacterDataWrapper character = invocation.getArgumentAt(0, CharacterDataWrapper.class);
            if (character.getId() == null) {
                character.setId(BigInteger.ONE.toString());
            }
            return character;
        });
    }

    /**
     * Test creating a new character and displaying the character sheet.
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        PluginDescription desc = desc1;
        String newCharacter = "{\"name\":\"Damien\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        RequestBuilder request = post("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/characters/")
                .content(newCharacter)
                .contentType(MediaType.APPLICATION_JSON_UTF8);


        MvcResult result = mvc.perform(request)
                .andDo(print()).andReturn();
        verify(characters).save(isA(CharacterDataWrapper.class));
    }

    /**
     * Test attempting to create a character for a firstPlugin that isn't present
     *
     * @throws Exception
     */
    @Test
    public void testCreateForMissingPlugin() throws Exception {
        String newCharacter = "{\"name\":\"Damien\"}";
        RequestBuilder request = post("/games/" +
                URLEncoder.encode("Missing", "UTF-8") + "/" +
                URLEncoder.encode("Missing", "UTF-8") + "/" +
                URLEncoder.encode("Missing", "UTF-8") + "/" +
                "characters" + "/")
                .content(newCharacter)
                .contentType(MediaType.APPLICATION_JSON_UTF8);

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    /**
     * Test getting a character.
     *
     * @throws Exception
     */
    @Test
    public void testGetCharacter() throws Exception {
        PluginDescription desc = desc1;
        String existingCharacter = "{\"name\" : \"Damien\"}";
        CharacterDataWrapper characterWrapper = new CharacterDataWrapper(desc, null, existingCharacter);
        characterWrapper.setId(BigInteger.ONE.toString());
        when(characters.findOne(BigInteger.ONE.toString())).thenReturn(characterWrapper);

        RequestBuilder request = get("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                "characters" + "/" +
                characterWrapper.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8_VALUE);

        MvcResult result = mvc.perform(request)
                .andDo(print())
                .andReturn();

        assertEquals(new ObjectMapper().writeValueAsString(characterWrapper), result.getResponse().getContentAsString());

        verify(characters).findOne(characterWrapper.getId());
    }

    /**
     * Test getting a character that doesn't exist
     *
     * @throws Exception
     */
    @Test
    public void testGetMissingCharacter() throws Exception {
        PluginDescription desc = desc1;


        RequestBuilder request = get("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                2)
                .contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andDo(print());
    }

    @Test
    public void testSaveCharacter() throws Exception {
        PluginDescription desc = desc1;
        String mockCharacter = "{\"name\":\"Damien\"}";
        BigInteger id = BigInteger.ONE;

        RequestBuilder request = put("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/characters/" +
                id.toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mockCharacter);

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isAccepted());
        CharacterDataWrapper characterDataWrapper = new CharacterDataWrapper(desc, 1L, mockCharacter);
        characterDataWrapper.setId("1");

        verify(characters).save(characterDataWrapper);
    }

    @Test
    public void testDeleteCharacter() throws Exception {
        PluginDescription desc = desc1;
        String id = BigInteger.ONE.toString();

        RequestBuilder request = delete("/games/" +
                URLEncoder.encode(desc.getAuthor(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getSystem(), "UTF-8") + "/" +
                URLEncoder.encode(desc.getVersion(), "UTF-8") + "/" +
                "characters" + "/" +
                id);

        mvc.perform(request)
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(characters).delete(id);
    }
}