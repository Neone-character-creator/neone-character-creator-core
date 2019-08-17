package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.authentication.User;

import javax.persistence.*;

/**
 * An association between an app user account and a third party OAuth account.
 * <p>
 * Created by Damien on 10/19/2016.
 */
@Entity
@Table(name = "oauth_accounts")
public class OAuthAccountAssociation {
    @Id
    @GeneratedValue
    private Integer id;
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    private User user;
    private String provider;
    private String oauthId;

    public OAuthAccountAssociation() {
    }

    public OAuthAccountAssociation(String associatedAccountName, String associatedAccountId) {
        this.provider = associatedAccountName;
        this.oauthId = associatedAccountId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getOauthId() {
        return oauthId;
    }

    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }
}
