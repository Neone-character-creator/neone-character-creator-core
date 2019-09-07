package io.github.thisisnozaku.charactercreator.authentication;

import io.github.thisisnozaku.charactercreator.data.OAuthAccountAssociation;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Damien on 11/13/2015.
 */
@Entity
@Table(name = "app_users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    /**
     * OAuth provider and accounts ids for this user.
     */
    @OneToMany
    private List<OAuthAccountAssociation> associatedAccounts;

    /**
     * Private constructor for hibernate.
     */
    private User(){}

    public User(Long id, List<OAuthAccountAssociation> associatedAccounts) {
        this.id = id;
        this.associatedAccounts = associatedAccounts.stream().map(e -> {
            e.setUser(this);
            return e;
        }).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public List<OAuthAccountAssociation> getAssociatedAccounts() {
        return associatedAccounts;
    }
}
