package io.github.thisisnozaku.charactercreator.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ThirdPartyOauthAuthentication extends AbstractAuthenticationToken {

    private final User principal;
    private final String credential;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public ThirdPartyOauthAuthentication(User principal, String credential, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credential = credential;
    }

    @Override
    public Object getCredentials() {
        return credential;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
