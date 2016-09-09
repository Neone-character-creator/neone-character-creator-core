package io.github.thisisnozaku.charactercreator.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by Damien on 11/13/2015.
 */
@Entity
@Table(name = "users")
@Component
public class User implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private Collection<? extends AppAuthority> authorities;
    private boolean enabled = false;
    private boolean accountNonExpired = true;
    private boolean credentialsNonExpired  = true;
    private boolean accountNonLocked  = true;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User() {
        this("", "", false, true, true, true, new HashSet<AppAuthority>());
    }

    public User(String username, String password, Collection<? extends AppAuthority> authorities) {
        this(username, password, false, true, true, true, authorities);
    }

    public User(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends AppAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
    }

    @OneToMany(targetEntity = AppAuthority.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return new ArrayList<GrantedAuthority>(authorities);
    }

    @Column(name = "password", nullable = false)
    @Override
    public String getPassword() {
        return password;
    }

    @Column(name = "username", nullable = false)
    @Override
    public String getUsername() {
        return username;
    }

    @Column(name = "account_not_expired", nullable = false)
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Column(name = "account_not_locked", nullable = false)
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Column(name = "credentials_not_expired", nullable = false)
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Column(name = "enabled", nullable = false)
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthorities(Collection<? extends AppAuthority> authorities) {
        this.authorities = authorities;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAccountNonExpired(boolean notExpired) {
        this.accountNonExpired = notExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (enabled != user.enabled) return false;
        if (accountNonExpired != user.accountNonExpired) return false;
        if (credentialsNonExpired != user.credentialsNonExpired) return false;
        if (accountNonLocked != user.accountNonLocked) return false;
        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (username != null ? !username.equals(user.username) : user.username != null) return false;
        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        return authorities != null ? authorities.equals(user.authorities) : user.authorities == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (authorities != null ? authorities.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (accountNonExpired ? 1 : 0);
        result = 31 * result + (credentialsNonExpired ? 1 : 0);
        result = 31 * result + (accountNonLocked ? 1 : 0);
        return result;
    }
}
