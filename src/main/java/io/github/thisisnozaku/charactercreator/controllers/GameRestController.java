package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepository;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Damien on 2/8/2016.
 */
@RestController
@RequestMapping("games/{author}/{game}/{version:.+?}/characters")
public class GameRestController {
    private final UserRepository accounts;
    private final CharacterMongoRepository characters;
    private final PluginManager plugins;

    @Inject
    public GameRestController(CharacterMongoRepository characters, UserRepository accounts, PluginManager pluginManager) {
        this.accounts = accounts;
        this.characters = characters;
        this.plugins = pluginManager;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
    public Character create(Character character, Model model, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version) {
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
            CharacterDataWrapper wrapper = new CharacterDataWrapper(description, null, character);
            character = characters.save(wrapper).getCharacter();
            model.addAttribute("characterid", wrapper.getId());
        } else {
            throw new MissingPluginException();
        }
        model.addAttribute("author", author);
        model.addAttribute("game", game);
        model.addAttribute("version", version);
        model.addAttribute("contentUrl", String.format("%s-%s-%s-character", author, game, version));
        return character;
    }

    /**
     * Replaces the given characters in the database, if it exists and the user is authorized to access it.
     *
     * @param character the character to save
     */
    @RequestMapping(value = "/{id}  ", method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Character save(Character character, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        GamePlugin plugin;
        try {
            PluginDescription description = new PluginDescription(author, game, version);
            plugin = plugins.getPlugin(description).get();
            PluginDescription targetPluginDescription = new PluginDescription(author, game, version);
            if (!description.equals(targetPluginDescription)) {
                throw new CharacterPluginMismatchException(plugin.getPluginDescription(), targetPluginDescription);
            }
            CharacterDataWrapper wrapper = new CharacterDataWrapper(description, null, character);
            return characters.save(wrapper).getCharacter();
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException();
        }
    }

    /**
     * Removes the given characters, if it exists and the user it authorized to access it.
     *
     * @param id the id of the character to remove
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable BigInteger id) {
        characters.delete(id);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
    public List<Character> getAllForUserForPlugin(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version){
        PluginDescription pluginDescription = new PluginDescription(author,game,version);
        return characters.findByAccountForGame(null, pluginDescription).stream().map(CharacterDataWrapper::getCharacter).collect(Collectors.toList());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public Character getCharacter(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @PathVariable("id") BigInteger id){
        PluginDescription pluginDescription = new PluginDescription(author,game,version);
        return characters.findOne(id).getCharacter();
    }

    @ExceptionHandler(MissingPluginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String missingPlugin() {
        return "missing-plugin";
    }
}
