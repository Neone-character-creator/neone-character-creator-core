package io.github.thisisnozaku.charactercreator.controllers;

import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.util.json.Jackson;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


    @RequestMapping(value = "/login/google", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void googleLogin(@RequestBody String accessToken, HttpSession session) throws IOException{
        GoogleCredential credentials = new GoogleCredential().setAccessToken(accessToken);
        Plus googlePlus = new Plus.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credentials)
                .setApplicationName("NEOne Character Builder")
                .build();
        Plus.People.Get get = googlePlus.people().get("me");
        Person me = get.execute();

        Authentication authentication = new UsernamePasswordAuthenticationToken(me.getId(), null, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
    }

    @RequestMapping(value = "/logout/google", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void googleLogout(){
        SecurityContextHolder.getContext().setAuthentication(null);
    }

}
