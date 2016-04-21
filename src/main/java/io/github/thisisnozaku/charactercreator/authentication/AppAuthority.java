package io.github.thisisnozaku.charactercreator.authentication;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

/**
 * Created by Damien on 2/28/2016.
 */
@Entity
@Table(name="authorities")
public class AppAuthority implements GrantedAuthority {
    private Integer id;
    private String authority;

    public AppAuthority(Integer id, String authority) {
        this.id = id;
        this.authority = authority;
    }

    public AppAuthority() {
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Column(name="authority_id", nullable = false)
    @Id
    @GeneratedValue
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
