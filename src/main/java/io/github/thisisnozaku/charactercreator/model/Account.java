package io.github.thisisnozaku.charactercreator.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by Damien on 11/13/2015.
 */
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private final String username;

    public Account(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if(this.id == null) {
            throw new IllegalStateException("Attempted to reassign the id of a user.");
        }
        this.id = id;
    }
}
