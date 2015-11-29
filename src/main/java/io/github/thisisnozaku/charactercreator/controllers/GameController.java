package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.data.AccountRepository;
import io.github.thisisnozaku.charactercreator.data.CharacterDao;
import io.github.thisisnozaku.charactercreator.model.Character;
import io.github.thisisnozaku.charactercreator.model.GamePlugin;
import io.github.thisisnozaku.charactercreator.model.PluginManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Created by Damien on 11/15/2015.
 */
@Controller
@RequestMapping("/{author}/{gamename}/{version:.+}")
public class GameController {
    private AccountRepository accounts;
    private CharacterDao characters;
    private PluginManager plugins;

    @Inject
    public GameController(CharacterDao characters, AccountRepository accounts, PluginManager plugins) {
        this.characters = characters;
        this.accounts = accounts;
        this.plugins = plugins;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "text/html")
    public String description(@PathVariable("author") String author, @PathVariable("gamename") String game, @PathVariable("version") String version) {
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
                return "missing-character";
            }
        } catch (NoSuchElementException ex) {
            return "missing-plugin";
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
            model.addAttribute("character", character);
        } catch (NoSuchElementException ex) {
            return "missing-plugin";
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        return String.format("%s-%s-%s-character", author, game, version);
    }

    /**
     * Replaces the given characters in the database, if it exists and the user is authorized to access it.
     *
     * @param character
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.PUT)
    public Object save(Object character) {
        return null;
    }

    /**
     * Removes the given characters, if it exists and the user it authorized to access it.
     *
     * @param id
     */
    @RequestMapping(value = "/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable int id) {

    }
}
