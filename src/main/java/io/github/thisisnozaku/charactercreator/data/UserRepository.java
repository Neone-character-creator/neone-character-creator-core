package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.authentication.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by Damien on 11/15/2015.
 */
@Repository
public interface UserRepository extends JpaRepository<OAuthAccountAssociation, Integer> {
    OAuthAccountAssociation findByProviderAndOauthId(String provider, String oauthId);
}
