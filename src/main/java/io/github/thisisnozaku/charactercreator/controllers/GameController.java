package io.github.thisisnozaku.charactercreator.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created by Damien on 11/15/2015.
 */
@RestController
@RequestMapping("/{author}/{gamename}/{version}")
public class GameController {

    @RequestMapping(value="", method = RequestMethod.GET)
    public ModelAndView view(Model model){
        //Return character sheet
        return null;
    }

    @RequestMapping(value="/", method = RequestMethod.GET)
    public List<?> getAllCharacters(){
        return null;
    }

    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public Object getCharacter(@PathVariable int id) {
        return null;
    }

    @RequestMapping(value="/", method = RequestMethod.POST)
    public Object create(Object character){
        return null;
    }

    @RequestMapping(value = "/", method = RequestMethod.PUT)
    public Object save(Object character){
        return null;
    }

    @RequestMapping(value = "/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void delete(@PathVariable int id) {

    }
}
