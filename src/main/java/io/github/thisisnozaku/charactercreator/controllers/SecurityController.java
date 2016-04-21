package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.authentication.AppAuthority;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.authentication.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Damien on 1/31/2016.
 */
@Controller
public class SecurityController {
    @Inject
    private UserRepository users;
    @Inject
    private PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/createuser", method = RequestMethod.POST)
    public String register(User user, @RequestHeader(value = "referer", required = false)String referralUrl) {
        Collection<AppAuthority> authorities = new HashSet<>();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        authorities.add(new AppAuthority(null, "USER"));
        user.setAuthorities(authorities);
        users.save(user);
        return "index";
    }

    @RequestMapping(value = "/login")
    public String login(User user, Model model, @RequestHeader(value = "referer", required = false)String referralUrl){
        model.addAttribute("user", user);
        return "login";
    }
}
