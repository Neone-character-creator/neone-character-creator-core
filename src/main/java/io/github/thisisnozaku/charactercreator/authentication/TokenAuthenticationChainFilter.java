package io.github.thisisnozaku.charactercreator.authentication;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;
import io.github.thisisnozaku.charactercreator.data.OAuthAccountAssociation;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.security.RunAs;
import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Service
public class TokenAuthenticationChainFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(TokenAuthenticationChainFilter.class);
    @Value("${google.oauth2.client.clientId}}")
    private String googleOauthClientToken;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Inject
    private UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest rawRequest, ServletResponse rawResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequestWrapper) rawRequest;
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader != "") {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new JacksonFactory())
                    .setAudience(Collections.singletonList(googleOauthClientToken))
                    .build();
            String[] authorizationHeaderTokens = authorizationHeader.split(" ");
            try {
                GoogleIdToken googleIdToken = verifier.verify(authorizationHeaderTokens[1]);
                String subject = googleIdToken.getPayload().getSubject();
                Optional<OAuthAccountAssociation> existingAuthentication = userRepository.findByProviderAndOauthId("google", subject);
                SecurityContextHolder.getContext().setAuthentication(new ThirdPartyOauthAuthentication(
                        existingAuthentication.map(OAuthAccountAssociation::getUser).orElseGet(() -> {
                            OAuthAccountAssociation newAssociation = new OAuthAccountAssociation("google", subject);
                            User newUser = new User(Collections.singletonList(newAssociation));
                            userRepository.save(newAssociation);
                            return newUser;
                        }), authorizationHeaderTokens[1], Collections.EMPTY_LIST));
            } catch (GeneralSecurityException | IllegalArgumentException e) {
                logger.debug("Google id token was invalid.");
            }
        }
        chain.doFilter(rawRequest, rawResponse);
    }

    @Override
    public void destroy() {

    }
}
