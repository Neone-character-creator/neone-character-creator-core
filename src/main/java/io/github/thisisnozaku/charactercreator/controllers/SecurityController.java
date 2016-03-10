package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.authentication.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Damien on 1/31/2016.
 */
@Controller
public class SecurityController {
    @Inject
    UserRepository users;

    @RequestMapping(value = "/createuser", method = RequestMethod.POST)
    public String register(User user, @RequestHeader(value = "referer", required = false)String referralUrl) {
        users.save(user);
        return "index";
    }

    @RequestMapping(value = "/login")
    public String login(User user, Model model, @RequestHeader(value = "referer", required = false)String referralUrl){
        model.addAttribute("user", user);
        return "login";
    }
}
