package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.authentication.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Damien on 11/15/2015.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    public User findByUsername(String username);
}
