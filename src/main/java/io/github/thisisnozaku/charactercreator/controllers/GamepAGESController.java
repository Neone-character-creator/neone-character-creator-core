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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Controller for handling access to game plugin pages.
 * <p>
 * Created by Damien on 11/15/2015.
 */
@Controller
@RequestMapping("games/{author}/{game}/{version:.+}")
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

    @RequestMapping(value = {"/", ""})
    public String redirect(HttpServletRequest request, @PathVariable("version") String version) {
        if (!request.getRequestURI().endsWith("/")) {
            return "redirect:" + version + "/pages/character";
        }
        return "redirect:pages/character";
    }

    @RequestMapping(value = "/pages/info", method = RequestMethod.GET, produces = "text/html")
    public String description(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model, HttpServletRequest request) throws UnsupportedEncodingException {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        GamePlugin plugin;
        try {
            plugin = plugins.getPlugin(URLDecoder.decode(author, "UTF-8"), URLDecoder.decode(game, "UTF-8"), URLDecoder.decode(version, "UTF-8")).get();
            return String.format("%s-%s-%s-description", author, game, version);
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException(new PluginDescription(author, game, version));
        }
    }

    @RequestMapping(value = "/pages/character/{id}", method = RequestMethod.GET, produces = "text/html")
    public String getCharacter(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model, @PathVariable String id, @AuthenticationPrincipal User user) {
        try {

            PluginDescription description = new PluginDescription(URLDecoder.decode(author, "UTF-8"),
                    URLDecoder.decode(game, "UTF-8"),
                    URLDecoder.decode(version, "UTF-8"));
            Optional<GamePlugin> plugin = plugins.getPlugin(description);
            if (!plugin.isPresent()) {
                throw new MissingPluginException(description);
            }
            String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
            CharacterDataWrapper wrapper;
            if (id != null) {
                wrapper = characters.findOne(id);
            } else {
                wrapper = new CharacterDataWrapper(description, currentUserId, null);
            }
            model.addAttribute("wrapper", wrapper);
            model.addAttribute("author", author);
            model.addAttribute("game", game);
            model.addAttribute("version", version);
            model.addAttribute("contentUrl", Paths.get(URLEncoder.encode(author, "UTF-8"), URLEncoder.encode(game, "UTF-8"), URLEncoder.encode(version, "UTF-8"), "pluginresource" ,plugin.get().getCharacterViewResourceName()));
            model.addAttribute("saveEnabled", true);
            model.addAttribute("deleteEnabled", true);
            model.addAttribute("exportEnabled", true);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        return "plugin-character-page";
    }

    @RequestMapping(value = {"/pages/character"}, method = RequestMethod.GET, produces = "text/html")
    public String getNewCharacter(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model, @AuthenticationPrincipal User user) {
        return getCharacter(author, game, version, model, null, user);
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