package io.github.thisisnozaku.charactercreator.data;

import com.sun.webkit.plugin.PluginManager;
import io.github.thisisnozaku.charactercreator.NeoneCoreApplication;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepository;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.math.BigInteger;

/**
 * Created by Damien on 1/19/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {NeoneCoreApplication.class})
public class CharacterMongoRepositoryTest {
    @Inject
    private CharacterMongoRepository repository;
    static WebApplicationContext applicationContext;
    @Inject

    @AfterClass
    public static void tearDown(){
        applicationContext.getAutowireCapableBeanFactory().destroyBean(applicationContext.getAutowireCapableBeanFactory().getBean("pluginManagerImpl"));
    }

    @Test
    public void testGetCharacter() {
        Character character = new Character() {
            private BigInteger id;
            private PluginDescription pluginDescription;

            @Override
            public BigInteger getId() {
                return id;
            }

            @Override
            public void setId(BigInteger id) {
                this.id = id;
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
        repository.save(character);
    }

    @Bean
    public PluginManager PluginManager(){
        return Mockito.mock(PluginManager.class);
    }

    @Inject
    public void setApplicationContext(WebApplicationContext context){
        applicationContext = context;
    }
}