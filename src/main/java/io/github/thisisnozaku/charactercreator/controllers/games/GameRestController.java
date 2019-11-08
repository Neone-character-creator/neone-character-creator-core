package io.github.thisisnozaku.charactercreator.controllers.games;

import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepositoryCustom;
import io.github.thisisnozaku.charactercreator.data.pdf.PdfCache;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import io.github.thisisnozaku.charactercreator.plugins.PluginWrapper;
import io.github.thisisnozaku.pdfexporter.DefaultPdfWriter;
import io.github.thisisnozaku.pdfexporter.JsonFieldValueExtractor;
import io.github.thisisnozaku.pdfexporter.PdfExporter;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the rest api.
 */
@RestController
@RequestMapping("/games/")
public class GameRestController {
    private final CharacterMongoRepositoryCustom characters;
    private final PluginManager<GamePlugin<Character>> plugins;
    private final Logger logger = LoggerFactory.getLogger(GameRestController.class);
    private final ObjectMapper objectMapper;
    private final PdfCache pdfDataCache;
    @Inject
    public GameRestController(CharacterMongoRepositoryCustom characters,
                              PluginManager<GamePlugin<Character>> pluginManager, ObjectMapper objectMapper,
                              PdfCache pdfCache) {
        this.characters = characters;
        this.plugins = pluginManager;
        this.objectMapper = objectMapper;
        this.pdfDataCache = pdfCache;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<PluginDescription> getAvailablePlugins() {
        return plugins.getAllPluginDescriptions();
    }

    /**
     * Create a new character for an authorized user.
     *
     * @param requestBody
     * @param author
     * @param game
     * @param version
     * @return
     */
    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters", method = RequestMethod.POST, produces = "application/json")
    public
    @ResponseBody
    CharacterDataWrapper create(HttpEntity<String> requestBody, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Creating a new character for user {} using plugin {}, {}, {}", currentUser.getId(), author, game, version);
        PluginDescription description = new PluginDescription(author, game, version);
        Optional<GamePlugin<Character>> plugin = plugins.getPlugin(description);
        if (plugin.isPresent()) {
            logger.info("Plugin {}, {}, {} found.", author, game, version);
            CharacterDataWrapper wrapper = new CharacterDataWrapper(description, currentUser.getId(), requestBody.getBody());
            wrapper = characters.save(wrapper);
            logger.info("Character (id {}) was saved.", wrapper.getId());
            return wrapper;
        } else {
            logger.info("Plugin {} - {} - {} was requested but unavailable.", author, game, version);
            throw new MissingPluginException(description);
        }
    }

    /**
     * Replaces the given characters in the database, if it exists and the user is authorized to access it.
     *
     * @param character the character to save
     */
    @Secured({"ROLE_USER"})
    @RequestMapping(value = "{author}/{game}/{version:.+?}/characters/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CharacterDataWrapper save(HttpEntity<String> character, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @PathVariable String id) {
        try {
            String decodedAuthor = URLDecoder.decode(author, "UTF-8");
            String decodedGame = URLDecoder.decode(game, "UTF-8");
            String decodedVersion = URLDecoder.decode(version, "UTF-8");

            PluginDescription targetPluginDescription = new PluginDescription(decodedAuthor, decodedGame, decodedVersion);
            return plugins.getPlugin(targetPluginDescription).map(pluginWrapper -> {
                User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                logger.info("Saving a character for user {} using plugin {}, {}, {}", currentUser.getId(), decodedAuthor, decodedGame, decodedVersion);
                CharacterDataWrapper wrapper = new CharacterDataWrapper(targetPluginDescription, currentUser.getId(), character.getBody());
                wrapper.setId(id);
                logger.info("Character id {} was saved.", wrapper.getId());
                return characters.save(wrapper);
            }).orElseThrow(() -> new MissingPluginException(targetPluginDescription));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Removes the given characters, if it exists and the user it authorized to access it.
     *
     * @param id the id of the character to remove
     */
    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable String id, @PathVariable String author, @PathVariable String game, @PathVariable String version) {
        characters.delete(id);
        logger.info("Character (id {}) for plugin {}, {}, {} was deleted.", id, author, game, version);
    }

    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters", method = RequestMethod.GET, produces = "application/json")
    public List<CharacterDataWrapper> getAllForUserForPlugin(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version) {
        PluginDescription pluginDescription = new PluginDescription(author, game, version);
        //TODO: Lookg into making user injectable via the method arguments.
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Getting all characters for user {} and plugin {}, {}, {}.", currentUser.getId(), author, game, version);
        List<CharacterDataWrapper> result = characters.findByUserAndPlugin(currentUser.getId(), pluginDescription);
        logger.info("Found {} characters.", result.size());
        return result;
    }

    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/pdf", method = RequestMethod.POST)
    public ResponseEntity<String> exportToPdf(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, RequestEntity<String> request) {
        try {
            PluginDescription pluginDescription = new PluginDescription(author, game, version);
            Optional<GamePlugin<Character>> plugin = plugins.getPlugin(pluginDescription);
            if (plugin.isPresent()) {
                Optional<InputStream> contentStream = plugins.getPluginResource(pluginDescription, "pdf").map(uri -> {
                    try {
                        return uri.toURL().openStream();
                    } catch (IOException ex) {
                        return null;
                    }
                });
                if (contentStream.isPresent()) {
                    InputStream resourceStream = contentStream.get();
                    Map<String, String> overrideMappings = plugins.getPluginResource(pluginDescription, "fields.json")
                            .map(fm -> {
                                try {
                                    return objectMapper.<Map<String, String>>readValue(fm.toURL(), new TypeReference<Map<String, String>>(){});
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }).orElse(Collections.EMPTY_MAP);
                    PdfExporter<String> pdfExporter = new PdfExporter<>(new DefaultPdfWriter(), new JsonFieldValueExtractor());
                    ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();

                    String characterJson = URLDecoder.decode(request.getBody(), "UTF-8");
                    pdfExporter.exportPdf(characterJson, resourceStream, pdfOut, overrideMappings);
                    HttpHeaders headers = new HttpHeaders();
                    String pdfUuid = pdfDataCache.addToCache(pdfOut.toByteArray());
                    return new ResponseEntity<>(pdfUuid, headers, HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                throw new MissingPluginException(pluginDescription);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/pdf/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> exportToPdf(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @PathVariable("uuid") String uuid) {
        Optional<byte[]> data = pdfDataCache.getFromCache(uuid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        return data.map(d -> new ResponseEntity<byte[]>(d, headers, HttpStatus.OK))
                .orElse(new ResponseEntity<byte[]>(null, headers, HttpStatus.NOT_FOUND));
    }

    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/{id}", method = RequestMethod.GET, produces = "application/json")
    public CharacterDataWrapper getCharacter(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @PathVariable("id") String id) {
        return characters.findOne(id);
    }



    @SuppressWarnings("SameReturnValue")
    @ExceptionHandler(MissingPluginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String missingPlugin() {
        return "missing-plugin";
    }
}
