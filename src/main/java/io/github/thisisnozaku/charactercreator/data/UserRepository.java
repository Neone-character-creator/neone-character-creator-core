package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.authentication.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Damien on 11/15/2015.
 */
@Repository
public interface UserRepository extends JpaRepository<OAuthAccountAssociation, Integer> {
    Optional<OAuthAccountAssociation> findByProviderAndOauthId(String provider, String oauthId);
}
