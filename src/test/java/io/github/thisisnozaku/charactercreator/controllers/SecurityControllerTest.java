package io.github.thisisnozaku.charactercreator.controllers;

import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.common.net.MediaType;
import io.github.thisisnozaku.charactercreator.authentication.GoogleOAuthUserResolver;
import io.github.thisisnozaku.charactercreator.data.OAuthAccountAssociation;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.plugins.PluginResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareEverythingForTest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.annotation.MapMethodProcessor;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Created by Damie on 2/28/2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({GoogleOAuthUserResolver.class, Plus.Builder.class})
public class SecurityControllerTest {
    MockMvc mvc;
    UserRepository userRepository;
    SecurityController controller;

    @Before
    public void setup() throws Exception {
        userRepository = Mockito.mock(UserRepository.class);
        controller = new SecurityController(userRepository);
        MockMvcBuilder mvcBuilder = MockMvcBuilders.standaloneSetup(controller);
        mvc = mvcBuilder.build();

        Plus.Builder mockBuilder = PowerMockito.mock(Plus.Builder.class);
        PowerMockito.whenNew(Plus.Builder.class).withAnyArguments().thenReturn(mockBuilder);

        Plus.People mockPeople = PowerMockito.mock(Plus.People.class);
        Plus.People.Get mockGet = mock(Plus.People.Get.class);
        PowerMockito.when(mockPeople.get("me")).thenReturn(mockGet);

        Person me = new Person();
        PowerMockito.suppress(mockGet.getClass().getMethod("execute"));
        PowerMockito.when(mockGet, "execute").thenReturn(me);

        Plus mockApi = mock(Plus.class);
        PowerMockito.when(mockBuilder, "build").thenReturn(mockApi);
        when(mockApi.people()).thenReturn(mockPeople);

        // The Person class that the google oauth library uses is a subtype of Map. One of the argument resolvers that
        // Spring automatically adds to the chain the mock mvc uses accepts Maps and is registered before the OAuth
        // resolver. Because the mock mvc provide no API to modify the resolver, we're forced to use reflection.
        try {
            Field servletField = MockMvc.class.getDeclaredField("servlet");
            servletField.setAccessible(true);
            Field handlerAdapterField = DispatcherServlet.class.getDeclaredField("handlerAdapters");
            handlerAdapterField.setAccessible(true);
            ((RequestMappingHandlerAdapter) ((List) handlerAdapterField.get(
                    servletField.get(mvc)
            )).get(0)).setArgumentResolvers(Stream.concat(Stream.of(new GoogleOAuthUserResolver()), ((RequestMappingHandlerAdapter) ((List) handlerAdapterField.get(
                    servletField.get(mvc)
            )).get(0)).getArgumentResolvers().stream().filter(e -> {
                return !(e instanceof MapMethodProcessor);
            })).collect(Collectors.toList()));
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Login with a new account.
     */
    @Test
    public void googleLogin() throws Exception {
        mvc.perform(
                MockMvcRequestBuilders.post("/login/google").content("1234567890").contentType(MediaType.JSON_UTF_8.toString())
        ).andExpect(
                MockMvcResultMatchers.status().is(200)
        );
        verify(userRepository, times(1)).saveAndFlush(isA(OAuthAccountAssociation.class));
    }

    @Test
    public void googleLogout() throws Exception {
        fail();
    }

    @Bean
    public UserRepository userRepository() {
        return userRepository;
    }

    @Bean
    public static PluginResourceResolver pluginResourceResolver() {
        return Mockito.mock(PluginResourceResolver.class);
    }

}