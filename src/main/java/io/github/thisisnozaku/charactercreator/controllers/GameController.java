package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepository;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingCharacterException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Controller for handling access to game plugin pages.
 * <p>
 * Created by Damien on 11/15/2015.
 */
@Controller
@RequestMapping("games/{author}/{game}/{version:.+}")
public class GameController {
    private final UserRepository accounts;
    private final CharacterMongoRepository characters;
    private final PluginManager plugins;

    @Inject
    public GameController(CharacterMongoRepository characters, UserRepository accounts, PluginManager plugins) {
        this.characters = characters;
        this.accounts = accounts;
        this.plugins = plugins;
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = "text/html")
    public String description(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model) throws UnsupportedEncodingException {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            plugins.getPlugin(URLDecoder.decode(author, "UTF-8"), URLDecoder.decode(game, "UTF-8"), URLDecoder.decode(version, "UTF-8")).get();
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException();
        }
        model.addAttribute("author", author);
        model.addAttribute("game", game);
        model.addAttribute("version", version);
        model.addAttribute("contentUrl", String.format("%s-%s-%s-description", author, game, version));
        return "plugin-character-page";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getCharacter(Character character, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @PathVariable BigInteger id, Model model) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");

        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        PluginDescription targetPlugin = new PluginDescription(author, game, version);
        try {
            character = characters.findOne(id);
            if (character == null) {
                throw new MissingCharacterException();
            } else if (!character.getPluginDescription().equals(targetPlugin)) {
                throw new CharacterPluginMismatchException(character.getPluginDescription(), targetPlugin);
            }
            model.addAttribute("character", character);
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException();
        }
        model.addAttribute("author", author);
        model.addAttribute("game", game);
        model.addAttribute("version", version);
        model.addAttribute("contentUrl", String.format("%s-%s-%s-character", author, game, version));
        model.addAttribute("saveEnabled", true);
        model.addAttribute("deleteEnabled", true);
        return "plugin-character-page";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String getNewCharacter(Character character, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            model.addAttribute("character", character);
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException();
        }
        model.addAttribute("author", author);
        model.addAttribute("game", game);
        model.addAttribute("version", version);
        model.addAttribute("contentUrl", String.format("%s-%s-%s-character", author, game, version));
        model.addAttribute("saveEnabled", true);
        model.addAttribute("deleteEnabled", true);
        return "plugin-character-page";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public Character create(Character character, Model model, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        Optional<GamePlugin> plugin = plugins.getPlugin(author, game, version);
        if (plugin.isPresent()) {
            character.setPluginDescription(plugin.get().getPluginDescription());
            character = characters.save(character);
            model.addAttribute("character", character);
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
    @RequestMapping(value = "/{id}  ", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void save(Character character, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        GamePlugin plugin;
        try {
            plugin = plugins.getPlugin(character.getPluginDescription().getAuthor(), character.getPluginDescription().getSystem(), character.getPluginDescription().getVersion()).get();
            PluginDescription targetPluginDescription = new PluginDescription(author, game, version);
            if (!plugin.getPluginDescription().equals(targetPluginDescription)) {
                throw new CharacterPluginMismatchException(plugin.getPluginDescription(), targetPluginDescription);
            }
            characters.save(character);
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException();
        }
    }

    /**
     * Removes the given characters, if it exists and the user it authorized to access it.
     *
     * @param id the id of the character to remove
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable BigInteger id) {
        characters.delete(id);
    }

    @ExceptionHandler(MissingCharacterException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static String missingCharacter() {
        return "missing-character";
    }

    @ExceptionHandler(MissingPluginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static String missingPlugin() {
        return "missing-plugin";
    }

    @ExceptionHandler(CharacterPluginMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    private static ModelAndView wrongPluginForCharacter(CharacterPluginMismatchException ex) {
        ModelAndView mv = new ModelAndView();
        mv.addObject("expected", ex.getRequiredPlugin());
        mv.addObject("actual", ex.getActualPlugin());
        mv.setViewName("pluginw-mismatch");
        return mv;
    }
}