package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.NeoneCoreApplication;
import io.github.thisisnozaku.charactercreator.data.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * Created by Damien on 11/15/2015.
 */
@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {NeoneCoreApplication.class})
public class IndexControllerTest {
    MockMvc mvc;
    @InjectMocks
    IndexController controller;

    @Mock
    AccountRepository accounts;

    @Before
    public void setup(){
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