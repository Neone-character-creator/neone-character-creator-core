package io.github.thisisnozaku.charactercreator.model;

/**
 * Created by Damien on 11/13/2015.
 */
public class User {
    private Long id;
    private final String username;

    public User(String username) {
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
