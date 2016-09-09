package io.github.thisisnozaku.charactercreator.authentication;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Damien on 9/2/2016.
 */
@Entity
public class ActivationToken {
    private Long id;
    @Id
    private String token;

    public ActivationToken() {
    }

    public ActivationToken(Long id, String token) {
        this.id = id;
        this.token = token;
    }

    public Long getid() {
        return id;
    }

    public void setid(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
