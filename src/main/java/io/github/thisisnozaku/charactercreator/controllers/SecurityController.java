package io.github.thisisnozaku.charactercreator.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.model.Person;
import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.authentication.google.GoogleOauthAuthorizationPrompt;
import io.github.thisisnozaku.charactercreator.data.OAuthAccountAssociation;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Damien on 1/31/2016.
 */
@Controller
public class SecurityController {
    private Logger logger = LoggerFactory.getLogger(SecurityController.class);
    @Value("${google.oauth2.client.clientId:n/a}")
    private String googleClientId;
    @Value("${google.oauth2.client.clientSecret:n/a}")
    private String googleClientSecret;
    @Value("${google.oauth2.redirectUri:n/a}")
    private String googleRedirectUri;

    @Value("${google.oauth2.authUrl}")
    private String googleAuthUrl;
    @Inject
    private UserRepository users;

    public SecurityController(UserRepository users) {
        this.users = users;
    }

    @RequestMapping(value = "/login/google", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void googleLogin(Person googleUser) throws IOException {
        OAuthAccountAssociation accountAssociation = users.findByProviderAndOauthId("google", googleUser.getResourceName()).orElse(null);
        if (accountAssociation == null) {
            accountAssociation = new OAuthAccountAssociation("google", googleUser.getResourceName());
            accountAssociation.setUser(new User(null, Arrays.asList(accountAssociation)));
            accountAssociation = users.save(accountAssociation);
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(accountAssociation.getUser(), null, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
    }

    @RequestMapping(value = "/logout/google", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void googleLogout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @RequestMapping(value = "/login/google/external/begin", method = RequestMethod.POST)
    @CrossOrigin
    public ResponseEntity<GoogleOauthAuthorizationPrompt> externalGoogleLogin() {
        GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow(
                new NetHttpTransport(),
                new JacksonFactory(),
                googleClientId,
                googleClientSecret,
                Arrays.asList("email", "openid")
        );

        GoogleAuthorizationCodeRequestUrl googleAuthorizationCodeRequestUrl = googleAuthorizationCodeFlow.newAuthorizationUrl();
        googleAuthorizationCodeRequestUrl.setApprovalPrompt("auto");

        return ResponseEntity.ok(new GoogleOauthAuthorizationPrompt(googleAuthorizationCodeRequestUrl.build()));
    }
}
