package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.Character;
import io.github.thisisnozaku.charactercreator.GamePlugin;
import io.github.thisisnozaku.charactercreator.PluginDescription;
import io.github.thisisnozaku.charactercreator.PluginManager;
import io.github.thisisnozaku.charactercreator.data.AccountRepository;
import io.github.thisisnozaku.charactercreator.data.CharacterDao;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingCharacterException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Controller for handling access to game plugin pages.
 * <p>
 * Created by Damien on 11/15/2015.
 */
@Controller
@RequestMapping("/{author}/{gamename}/{version:.+}")
public class GameController {
    private final AccountRepository accounts;
    private final CharacterDao characters;
    private final PluginManager plugins;

    @Inject
    public GameController(CharacterDao characters, AccountRepository accounts, PluginManager plugins) {
        this.characters = characters;
        this.accounts = accounts;
        this.plugins = plugins;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "text/html")
    public String description(@PathVariable("author") String author, @PathVariable("gamename") String game, @PathVariable("version") String version) throws UnsupportedEncodingException {
        try {
            plugins.getPlugin(URLDecoder.decode(author, "UTF-8"), URLDecoder.decode(game, "UTF-8"), URLDecoder.decode(version, "UTF-8")).get();
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException();
        }
        return String.format("%s-%s-%s-description", author, game, version);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String getCharacter(@PathVariable("author") String author, @PathVariable("gamename") String game, @PathVariable("version") String version, @PathVariable long id, Model model) {
        try {
            Optional<GamePlugin> plugin = plugins.getPlugin(URLDecoder.decode(author, "UTF-8"), URLDecoder.decode(game, "UTF-8"), URLDecoder.decode(version, "UTF-8"));
            Character character = plugin.get().getNewCharacter();
            try {
                model.addAttribute("character", characters.getCharacter(id, character.getClass()).get());
            } catch (NoSuchElementException ex) {
                throw new MissingCharacterException();
            } catch (ClassCastException ex) {
                throw new CharacterPluginMismatchException(new PluginDescription(author, game, version), plugin.get().getPluginDescription());
            }
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException();
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        return String.format("%s-%s-%s-character", author, game, version);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String create(Model model, @PathVariable("author") String author, @PathVariable("gamename") String game, @PathVariable("version") String version) {
        try {
            Optional<GamePlugin> plugin = plugins.getPlugin(URLDecoder.decode(author, "UTF-8"), URLDecoder.decode(game, "UTF-8"), URLDecoder.decode(version, "UTF-8"));
            Character character = plugin.get().getNewCharacter();
            characters.createCharacter(character);
            model.addAttribute("character", character);
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException();
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        return String.format("%s-%s-%s-character", author, game, version);
    }

    /**
     * Replaces the given characters in the database, if it exists and the user is authorized to access it.
     *
     * @param character the character to save
     */
    @RequestMapping(value = "/{id}  ", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void save(Character character, @PathVariable("author") String author, @PathVariable("gamename") String game, @PathVariable("version") String version) {
        characters.updateCharacter(character);
    }

    /**
     * Removes the given characters, if it exists and the user it authorized to access it.
     *
     * @param id the id of the character to remove
     */
    @RequestMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable long id) {
        characters.deleteCharacter(id);
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
        mv.setViewName("plugin-mismatch");
        return mv;
    }
}