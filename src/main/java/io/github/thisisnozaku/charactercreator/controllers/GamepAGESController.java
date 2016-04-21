package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepository;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("games/{author}/{game}/{version:.+}/")
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

    @RequestMapping(value={"/", ""})
    public String redirect() {
        return "redirect:pages/info";
    }

    @RequestMapping(value = "/pages/info", method = RequestMethod.GET, produces = "text/html")
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
            throw new MissingPluginException(new PluginDescription(author, game, version));
        }
        model.addAttribute("author", author);
        model.addAttribute("game", game);
        model.addAttribute("version", version);
        model.addAttribute("contentUrl", String.format("%s-%s-%s-description", author, game, version));
        return "plugin-character-page";
    }

    @RequestMapping(value = "/pages/character", method = RequestMethod.GET, produces = "text/html")
    public String getCharacter(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model, @RequestParam(required = false) String id, @AuthenticationPrincipal User user) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        PluginDescription description = new PluginDescription(author, game, version);
        Optional<GamePlugin> plugin = plugins.getPlugin(author, game, version);
        if (!plugin.isPresent()) {
            throw new MissingPluginException(description);
        }
        CharacterDataWrapper wrapper;
        if (id != null) {
            wrapper = characters.findOne(id);
        } else {
            wrapper = new CharacterDataWrapper(description, user, null);
        }
        model.addAttribute("wrapper", wrapper);
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
    public String missingPlugin(Model model, MissingPluginException ex) {
        model.addAttribute("author", ex.getMissingPlugin().getAuthor());
        model.addAttribute("game", ex.getMissingPlugin().getSystem());
        model.addAttribute("version", ex.getMissingPlugin().getVersion());
        return "missing-plugin";
    }
}