package io.github.thisisnozaku.charactercreator.test.controllers;

import io.github.thisisnozaku.charactercreator.TestConfiguration;
import io.github.thisisnozaku.charactercreator.controllers.IndexController;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * Created by Damien on 11/15/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestConfiguration.class})
@TestPropertySource("classpath:/application-dev.properties")
public class IndexControllerTest {
    MockMvc mvc;

    @Inject
    IndexController controller;

    @Mock
    UserRepository accounts;

    @Before
    public void setup(){
        initMocks(this);

        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Test displaying the index page to an unauthenticated user.
     */
    @Test
    public void testGettingIndexUnauthenticated() throws Exception {
        MockHttpServletRequestBuilder request = get("/").contentType(MediaType.TEXT_HTML);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}