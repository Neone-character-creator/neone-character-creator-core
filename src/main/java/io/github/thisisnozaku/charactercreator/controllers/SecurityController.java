package io.github.thisisnozaku.charactercreator.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.data.OAuthAccountAssociation;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Damien on 1/31/2016.
 */
@Controller
public class SecurityController {
    private Logger logger = LoggerFactory.getLogger(SecurityController.class);
    @Value("${google.oauth2.client.clientId?:n/a}")
    private String clientId;
    @Value("${google.oauth2.client.clientSecret:n/a}")
    private String clientSecret;
    @Inject
    private UserRepository users;

    public SecurityController(UserRepository users) {
        this.users = users;
    }

    @RequestMapping(value = "/login/google", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void googleLogin(Person googleUser, HttpSession session) throws IOException {
        OAuthAccountAssociation accountAssociation = users.findByProviderAndOauthId("google", googleUser.getId());
        User user = null;
        if (accountAssociation == null) {
            accountAssociation = new OAuthAccountAssociation("google", googleUser.getId());
            user = new User(null, Arrays.asList(accountAssociation));
            accountAssociation.setUser(user);
            users.saveAndFlush(accountAssociation);
        } else {
            user = accountAssociation.getUser();
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
    }

    @RequestMapping(value = "/logout/google", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void googleLogout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
