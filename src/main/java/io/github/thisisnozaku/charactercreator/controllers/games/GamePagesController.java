package io.github.thisisnozaku.charactercreator.controllers.games;

import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepository;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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
@RequestMapping("games/{author}/{game}/{version:.+}")
public class GamePagesController {
    private final UserRepository accounts;
    private final CharacterMongoRepository characters;
    private final PluginManager plugins;
    @Value("${google.oauth2.client.clientId}")
    private String googleClientId;

    @Inject
    public GamePagesController(CharacterMongoRepository characters, UserRepository accounts, PluginManager plugins) {
        this.characters = characters;
        this.accounts = accounts;
        this.plugins = plugins;
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

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "text/html")
    public String getCharacterSheet(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model, HttpServletRequest request, @AuthenticationPrincipal String currentUser) {
        try {
            PluginDescription description = new PluginDescription(URLDecoder.decode(author, "UTF-8"),
                    URLDecoder.decode(game, "UTF-8"),
                    URLDecoder.decode(version, "UTF-8"));
            Optional<GamePlugin> plugin = plugins.getPlugin(description);
            if (!plugin.isPresent()) {
                throw new MissingPluginException(description);
            }
            CharacterDataWrapper wrapper;
            if (model.containsAttribute("character-wrapper")) {
                wrapper = (CharacterDataWrapper) model.asMap().get("character-wrapper");
            } else {
                wrapper = new CharacterDataWrapper(description, currentUser, null);
            }
            model.addAttribute("wrapper", wrapper);
            model.addAttribute("author", author);
            model.addAttribute("game", game);
            model.addAttribute("version", version);
            model.addAttribute("authenticated", currentUser != null);
            model.addAttribute("saveEnabled", currentUser != null && wrapper.getCharacter() != null);
            model.addAttribute("deleteEnabled", currentUser != null && wrapper.getCharacter() != null);
            model.addAttribute("loadEnabled", currentUser != null);
            model.addAttribute("exportEnabled", true);
            model.addAttribute("google_client_id", googleClientId);
            model.addAttribute("contentUrl", String.format("%s:%s/pluginresource/%s/%s/%s", request.getLocalName(), request.getLocalPort(), author, game, version));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        return "plugin-character-page";
    }

    @RequestMapping(value = "/character/{id}", method = RequestMethod.GET, produces = "text/html")
    public String getCharacter(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, Model model, @PathVariable String id, RedirectAttributes redirectAttributes, @AuthenticationPrincipal String currentUser) {
        try {
            PluginDescription description = new PluginDescription(URLDecoder.decode(author, "UTF-8"),
                    URLDecoder.decode(game, "UTF-8"),
                    URLDecoder.decode(version, "UTF-8"));
            Optional<GamePlugin> plugin = plugins.getPlugin(description);
            if (!plugin.isPresent()) {
                throw new MissingPluginException(description);
            }
            CharacterDataWrapper wrapper;
            if (id != null) {
                wrapper = characters.findOne(id);
            } else {
                wrapper = new CharacterDataWrapper(description, currentUser, null);
            }
            redirectAttributes.addFlashAttribute("character-wrapper", wrapper);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        return String.format("redirect:/games/%s/%s/%s", author, game, version);
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