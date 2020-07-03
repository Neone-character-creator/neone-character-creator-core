package io.github.thisisnozaku.charactercreator.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for displaying the index page.
 * Created by Damien on 11/13/2015.
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "redirect:https://web.neonecharactercreatore.net";
    }
}
