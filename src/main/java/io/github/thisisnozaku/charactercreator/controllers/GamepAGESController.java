package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepository;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingCharacterException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for handling access to game plugin pages.
 * <p>
 * Created by Damien on 11/15/2015.
 */
@Controller
@RequestMapping("games/{author}/{game}/{version:.+}/pages")
public class GamePagesController {
    private final UserRepository accounts;
    private final CharacterMongoRepository characters;
    private final PluginManager plugins;

    @Inject
    public GamePagesController(CharacterMongoRepository characters, UserRepository accounts, PluginManager plugins) {
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

    @RequestMapping(value = "/character", method = RequestMethod.GET, produces = "text/html")
    public String getCharacter(Character character, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model, @RequestParam(required = false) BigInteger id) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        if (id != null) {
            PluginDescription description = new PluginDescription(author, game, version);
            Optional<GamePlugin> plugin = plugins.getPlugin(author, game, version);
            if (plugin.isPresent()) {
                CharacterDataWrapper wrapper = new CharacterDataWrapper(description, null, character);
                character = characters.findOne(id).getCharacter();
                model.addAttribute("characterid", wrapper.getId());
            } else {
                throw new MissingPluginException();
            }
        }
        model.addAttribute("character", character);
        model.addAttribute("author", author);
        model.addAttribute("game", game);
        model.addAttribute("version", version);
        model.addAttribute("contentUrl", String.format("%s-%s-%s-character", author, game, version));
        model.addAttribute("saveEnabled", true);
        model.addAttribute("deleteEnabled", true);
        return "plugin-character-page";
    }

    @ExceptionHandler(MissingPluginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String missingPlugin() {
        return "missing-plugin";
    }
}