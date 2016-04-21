package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepository;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepositoryCustom;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by Damien on 2/8/2016.
 */
@RestController
@RequestMapping("games/{author}/{game}/{version:.+?}/characters")
public class GameRestController {
    private final UserRepository accounts;
    private final CharacterMongoRepositoryCustom characters;
    private final PluginManager plugins;

    @Inject
    public GameRestController(CharacterMongoRepositoryCustom characters, UserRepository accounts, PluginManager pluginManager) {
        this.accounts = accounts;
        this.characters = characters;
        this.plugins = pluginManager;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
    public CharacterDataWrapper create(HttpEntity<String> requestBody, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @AuthenticationPrincipal User user) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        PluginDescription description = new PluginDescription(author, game, version);
        Optional<GamePlugin> plugin = plugins.getPlugin(author, game, version);
        if (plugin.isPresent()) {
            CharacterDataWrapper wrapper = new CharacterDataWrapper(description, user, requestBody.getBody());
            wrapper = characters.save(wrapper);
            return wrapper;
        } else {
            throw new MissingPluginException(description);
        }
    }

    /**
     * Replaces the given characters in the database, if it exists and the user is authorized to access it.
     *
     * @param character the character to save
     */
    @RequestMapping(value = "/{id}  ", method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CharacterDataWrapper save(HttpEntity<String> character, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @PathVariable String id) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        GamePlugin plugin;
        PluginDescription description = new PluginDescription(author, game, version);
        try {
            plugin = plugins.getPlugin(description).get();
            PluginDescription targetPluginDescription = new PluginDescription(author, game, version);
            if (!description.equals(targetPluginDescription)) {
                throw new CharacterPluginMismatchException(plugin.getPluginDescription(), targetPluginDescription);
            }
            CharacterDataWrapper wrapper = new CharacterDataWrapper(description, null, character.getBody());
            wrapper.setId(id);
            return characters.save(wrapper);
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException(description);
        }
    }

    /**
     * Removes the given characters, if it exists and the user it authorized to access it.
     *
     * @param id the id of the character to remove
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable String id) {
        characters.delete(id);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    public List<CharacterDataWrapper> getAllForUserForPlugin(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @AuthenticationPrincipal User principal) {
        PluginDescription pluginDescription = new PluginDescription(author, game, version);
        List<CharacterDataWrapper> result = characters.findByUserAndPlugin(principal, pluginDescription);
        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public CharacterDataWrapper getCharacter(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @PathVariable("id") String id) {
        PluginDescription pluginDescription = new PluginDescription(author, game, version);
        return characters.findOne(id);
    }

    @ExceptionHandler(MissingPluginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String missingPlugin() {
        return "missing-plugin";
    }
}
