package io.github.thisisnozaku.charactercreator.plugins;

import io.github.thisisnozaku.charactercreator.NeoneCoreApplication;
import io.github.thisisnozaku.charactercreator.data.AccountRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.BundleException;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Created by Damien on 12/2/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {NeoneCoreApplication.class, PluginManagerImplTest.class})
public class PluginManagerImplTest {
    @Inject
    private PluginManager manager;
    @Mock
    AccountRepository accounts;
    @Inject
    WebApplicationContext applicationContext;
    @Inject
    MockServletContext servletContext;
    MockMvc mvc;

    @Before
    public void setup() throws IOException, BundleException {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    public void testDescriptionViewResolution() throws Exception {
        PluginDescription desc = manager.getAllPluginDescriptions().iterator().next();
        RequestBuilder request = get(String.format("/games/%s/%s/%s", desc.getAuthorName(), desc.getSystemName(), desc.getVersion()));
        ResultActions result = mvc.perform(request);
        result.andDo(print())
                .andExpect(view().name(String.format("%s-%s-%s-description", desc.getAuthorName(), desc.getSystemName(), desc.getVersion())))
                .andReturn().getResponse().getContentAsString().contains("This is the information page for the test plugin.");
    }

    @Test
    public void testNewCharacterViewResolution() throws Exception {
        PluginDescription desc = manager.getAllPluginDescriptions().iterator().next();
        RequestBuilder request = get(String.format("/games/%s/%s/%s/", desc.getAuthorName(), desc.getSystemName(), desc.getVersion()));
        ResultActions result = mvc.perform(request);
        result.andDo(print())
                .andExpect(view().name(String.format("%s-%s-%s-character", desc.getAuthorName(), desc.getSystemName(), desc.getVersion())))
                .andReturn().getResponse().getContentAsString().contains("This is the information page for the test plugin.");
    }

    @Bean
    public AccountRepository mockAccounts() {
        return Mockito.mock(AccountRepository.class);
    }
}