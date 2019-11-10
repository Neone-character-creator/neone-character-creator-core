package io.github.thisisnozaku.charactercreator.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.github.thisisnozaku.charactercreator.data.OAuthAccountAssociation;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Service
public class TokenAuthenticationChainFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(TokenAuthenticationChainFilter.class);
    @Value("${google.oauth2.client.clientId}")
    private String googleOauthClientToken;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Inject
    private UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest rawRequest, ServletResponse rawResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequestWrapper) rawRequest;
        String authenticationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authenticationHeader != null && authenticationHeader != "") {
            try {
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                        new JacksonFactory())
                        .setAudience(Collections.singletonList(googleOauthClientToken))
                        .build();
                String[] authorizationHeaderTokens = authenticationHeader.split(" ");
                if(authorizationHeaderTokens.length == 2) {
                    GoogleIdToken googleIdToken = verifier.verify(authorizationHeaderTokens[1]);
                    String subject = googleIdToken.getPayload().getSubject();
                    Optional<OAuthAccountAssociation> existingAuthentication = userRepository.findByProviderAndOauthId("google", subject);
                    SecurityContextHolder.getContext().setAuthentication(new ThirdPartyOauthAuthentication(
                            existingAuthentication.map(OAuthAccountAssociation::getUser).orElseGet(() -> {
                                OAuthAccountAssociation newAssociation = new OAuthAccountAssociation("google", subject);
                                User newUser = new User(Collections.singletonList(newAssociation));
                                userRepository.save(newAssociation);
                                return newUser;
                            }), authorizationHeaderTokens[1], Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))));
                } else {
                    logger.warn("Authorization header present but didn't have 2 elements.");
                }
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
