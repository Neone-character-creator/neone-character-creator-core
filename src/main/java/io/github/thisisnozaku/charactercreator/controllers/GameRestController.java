package io.github.thisisnozaku.charactercreator.controllers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepositoryCustom;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.GamePlugin;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import io.github.thisisnozaku.pdfexporter.JsonFieldValueExtractor;
import io.github.thisisnozaku.pdfexporter.PdfExporter;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the rest api.
 */
@RestController
@RequestMapping("games/")
public class GameRestController {
    private final UserRepository accounts;
    private final CharacterMongoRepositoryCustom characters;
    private final PluginManager plugins;
    private final Cache<String, File> generatedPdfs;

    @Inject
    public GameRestController(CharacterMongoRepositoryCustom characters, UserRepository accounts, PluginManager pluginManager) {
        this.accounts = accounts;
        this.characters = characters;
        this.plugins = pluginManager;
        generatedPdfs = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES).build();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<PluginDescription> getAvailablePlugins(){
        return plugins.getAllPluginDescriptions();
    }

    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters", method = RequestMethod.POST, produces = "application/json")
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
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/{id}  ", method = RequestMethod.PUT, produces = "application/json")
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
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable String id) {
        characters.delete(id);
    }

    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters", method = RequestMethod.GET, produces = "application/json")
    public List<CharacterDataWrapper> getAllForUserForPlugin(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @AuthenticationPrincipal User principal) {
        PluginDescription pluginDescription = new PluginDescription(author, game, version);
        List<CharacterDataWrapper> result = characters.findByUserAndPlugin(principal, pluginDescription);
        return result;
    }

    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/pdf", method = RequestMethod.POST)
    public ResponseEntity<String> exportToPdf(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, RequestEntity<String> request) {
        UUID pdfId;
        try {
            PluginDescription pluginDescription = new PluginDescription(author, game, version);
            Optional<GamePlugin> plugin = plugins.getPlugin(pluginDescription);
            if (plugin.isPresent()) {
                URI resource = plugins.getPluginResource(pluginDescription, plugin.get().getCharacterSheetPdfResourceName());
                ResponseEntity<String> response;
                if (resource != null) {
                    InputStream pdfResource = resource.toURL().openStream();
                    PdfExporter pdfExporter = new PdfExporter();
                    pdfId = UUID.randomUUID();
                    File tempPdfPath = Paths.get(Files.createTempDir().getCanonicalPath(), pdfId.toString()).toFile();
                    OutputStream out = new FileOutputStream(tempPdfPath);
                    pdfExporter.exportPdf(new JsonFieldValueExtractor().generateFieldMappings(URLDecoder.decode(request.getBody(), "UTF-8")), pdfResource, out);
                    generatedPdfs.put(pdfId.toString(), tempPdfPath);
                    response = new ResponseEntity<>(pdfId.toString(), HttpStatus.OK);
                } else {
                    response = new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                return response;
            } else {
                throw new MissingPluginException(pluginDescription);
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/pdf/{id}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getPdf(@PathVariable("id") String id) {
        try {
            ResponseEntity<byte[]> responseEntity;
            File pdf = generatedPdfs.getIfPresent(id);
            if (pdf != null) {
                byte[] out = Files.toByteArray(pdf);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentDispositionFormData("attachment", id + ".pdf");
                headers.setContentType(MediaType.parseMediaType("application/pdf"));
                responseEntity = new ResponseEntity<>(out, headers, HttpStatus.OK);
            } else {
                responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return responseEntity;
        } catch (IOException e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/{id}", method = RequestMethod.GET, produces = "application/json")
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
