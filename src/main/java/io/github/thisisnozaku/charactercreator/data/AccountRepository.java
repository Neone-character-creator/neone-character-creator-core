package io.github.thisisnozaku.charactercreator.data;

import org.hibernate.validator.internal.engine.resolver.JPATraversableResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.social.facebook.api.Account;
import org.springframework.stereotype.Repository;

/**
 * Created by Damien on 11/15/2015.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

}
