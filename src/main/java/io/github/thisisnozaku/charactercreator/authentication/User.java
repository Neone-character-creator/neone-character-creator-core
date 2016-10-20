package io.github.thisisnozaku.charactercreator.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.*;

/**
 * Created by Damien on 11/13/2015.
 */
@Entity
@Table(name = "users")
@Component
public class User {
    private Long id;
    /**
     * Maps OAuth providers and the accounts ids for that provider associated with this account.
     */
    private final Map<String,String> associatedAccountIds;

    public User(Long id, Map<String,String>associatedAccountIds) {
        this.id = id;
        this.associatedAccountIds = associatedAccountIds;
    }

    public Long getId() {
        return id;
    }

    public Map<String, String> getAssociatedAccountIds() {
        return associatedAccountIds;
    }
}
