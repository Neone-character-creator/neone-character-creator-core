package io.github.thisisnozaku.charactercreator.authentication;

import io.github.thisisnozaku.charactercreator.data.OAuthAccountAssociation;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Damien on 11/13/2015.
 */
@Entity
@Table(name = "app_users")
@Component
public class User {
    @Id
    @GeneratedValue
    private Long id;
    /**
     * Maps OAuth providers and the accounts ids for that provider associated with this account.
     */
    @OneToMany
    private List<OAuthAccountAssociation> associatedAccounts;

    public User(Long id, List<OAuthAccountAssociation> associatedAccounts) {
        this.id = id;
        this.associatedAccounts = associatedAccounts.stream().map(e->{
            e.setUser(this);
            return e;
        }).collect(Collectors.toList());
    }

    public User(List<OAuthAccountAssociation> associatedAccounts) {
        this.associatedAccounts = associatedAccounts;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public List<OAuthAccountAssociation> getAssociatedAccounts() {
        return associatedAccounts;
    }
}
