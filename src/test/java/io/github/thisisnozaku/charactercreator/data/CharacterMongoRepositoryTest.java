package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.NeoneCoreApplication;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Damien on 1/19/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {NeoneCoreApplication.class})
public class CharacterMongoRepositoryTest {
    @Inject
    private CharacterMongoRepository repository;
    @Inject
    static WebApplicationContext applicationContext;

    @After
    public void tearDown() {
        applicationContext.getAutowireCapableBeanFactory().destroyBean(applicationContext.getAutowireCapableBeanFactory().getBean("pluginManagerImpl"));
    }

    @Test
    public void testGetCharacter() {
        CharacterOne character = new CharacterOne();
        CharacterTwo charactertwo = new CharacterTwo();
        character.setId(BigInteger.ONE);
        charactertwo.setId(BigInteger.TEN);
        repository.save(character);
        assertEquals(character, repository.findOne(character.getId()));
        repository.save(charactertwo);
        List<Character> characterList = repository.findAll();
        assertEquals(charactertwo, repository.findOne(charactertwo.getId()));
    }

    @Inject
    public void setApplicationContext(WebApplicationContext context) {
        applicationContext = context;
    }

    public static class CharacterOne implements io.github.thisisnozaku.charactercreator.plugins.Character {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CharacterOne character = (CharacterOne) o;

            if (id != null ? !id.equals(character.id) : character.id != null) return false;
            return pluginDescription != null ? pluginDescription.equals(character.pluginDescription) : character.pluginDescription == null;

        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (pluginDescription != null ? pluginDescription.hashCode() : 0);
            return result;
        }
    }

    public static class CharacterTwo implements io.github.thisisnozaku.charactercreator.plugins.Character {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CharacterTwo character = (CharacterTwo) o;

            if (id != null ? !id.equals(character.id) : character.id != null) return false;
            return pluginDescription != null ? pluginDescription.equals(character.pluginDescription) : character.pluginDescription == null;

        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (pluginDescription != null ? pluginDescription.hashCode() : 0);
            return result;
        }
    }

}