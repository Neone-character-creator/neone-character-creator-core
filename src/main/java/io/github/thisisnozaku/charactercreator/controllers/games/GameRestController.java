package io.github.thisisnozaku.charactercreator.controllers.games;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Files;
import io.github.thisisnozaku.charactercreator.data.CharacterDataWrapper;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoRepositoryCustom;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.exceptions.CharacterPluginMismatchException;
import io.github.thisisnozaku.charactercreator.exceptions.MissingPluginException;
import io.github.thisisnozaku.charactercreator.plugins.*;
import io.github.thisisnozaku.pdfexporter.DefaultPdfWriter;
import io.github.thisisnozaku.pdfexporter.JsonFieldValueExtractor;
import io.github.thisisnozaku.pdfexporter.PdfExporter;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CharacterMongoRepositoryCustom characters;
    private final PluginManager plugins;
    private final Cache<String, File> generatedPdfs;

    @Inject
    public GameRestController(CharacterMongoRepositoryCustom characters,PluginManager pluginManager) {
        this.characters = characters;
        this.plugins = pluginManager;
        generatedPdfs = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES).build();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<PluginDescription> getAvailablePlugins(){
        return plugins.getAllPluginDescriptions();
    }

    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody CharacterDataWrapper create(HttpEntity<String> requestBody, @PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @AuthenticationPrincipal String currentUser) {
        try {
            author = URLDecoder.decode(author, "UTF-8");
            game = URLDecoder.decode(game, "UTF-8");
            version = URLDecoder.decode(version, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        PluginDescription description = new PluginDescription(author, game, version);
        Optional<PluginWrapper> plugin = plugins.getPlugin(author, game, version);
        if (plugin.isPresent()) {
            CharacterDataWrapper wrapper = new CharacterDataWrapper(description, currentUser, requestBody.getBody());
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
    @Secured({"ROLE_USER"})
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
        PluginWrapper plugin;
        PluginDescription description = new PluginDescription(author, game, version);
        try {
            plugin = plugins.getPlugin(description).get();
            PluginDescription targetPluginDescription = new PluginDescription(author, game, version);
            if (!description.equals(targetPluginDescription)) {
                throw new CharacterPluginMismatchException(targetPluginDescription, targetPluginDescription);
            }
            CharacterDataWrapper wrapper = new CharacterDataWrapper(description, SecurityContextHolder.getContext().getAuthentication().getPrincipal(), character.getBody());
            wrapper.setId(id);
            return characters.save(wrapper);
        } catch (NoSuchElementException ex) {
            throw new MissingPluginException(description);
        }
    }

    /**
     * Removes the given characters, if it exists and the user it authorized to access it.
     *
     *@param id the id of the character to remove
     */
    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/{id}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable String id) {
        characters.delete(id);
    }

    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters", method = RequestMethod.GET, produces = "application/json")
    public List<CharacterDataWrapper> getAllForUserForPlugin(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version) {
        PluginDescription pluginDescription = new PluginDescription(author, game, version);
        String currentid = SecurityContextHolder.getContext().getAuthentication().getName();
        List<CharacterDataWrapper> result = characters.findByUserAndPlugin(currentid, pluginDescription);
        return result;
    }

    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/pdf", method = RequestMethod.POST)
    public ResponseEntity<String> exportToPdf(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, RequestEntity<String> request) {
        UUID pdfId;
        try {
            PluginDescription pluginDescription = new PluginDescription(author, game, version);
            Optional<PluginWrapper> plugin = plugins.getPlugin(pluginDescription);
            if (plugin.isPresent()) {
                InputStream resourceStream = plugin.get().getResourceAsStream("pdf");
                ResponseEntity<String> response;
                if (resourceStream != null) {
                    PdfExporter<String> pdfExporter = new PdfExporter(new DefaultPdfWriter(), new JsonFieldValueExtractor());
                    pdfId = UUID.randomUUID();
                    File tempPdfPath = Paths.get(Files.createTempDir().getCanonicalPath(), pdfId.toString()).toFile();
                    OutputStream out = new FileOutputStream(tempPdfPath);
                    String characterJson = URLDecoder.decode(request.getBody(), "UTF-8");
                    pdfExporter.exportPdf(characterJson, resourceStream, out);
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

    @Secured({"ROLE_USER"})
    @RequestMapping(value = "/{author}/{game}/{version:.+?}/characters/{id}", method = RequestMethod.GET, produces = "application/json")
    public CharacterDataWrapper getCharacter(@PathVariable("author") String author, @PathVariable("game") String game, @PathVariable("version") String version, @PathVariable("id") String id) {
        PluginDescription pluginDescription = new PluginDescription(author, game, version);
        CharacterDataWrapper wrapper = characters.findOne(id);
        return wrapper;
    }

    @ExceptionHandler(MissingPluginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String missingPlugin() {
        return "missing-plugin";
    }
}
