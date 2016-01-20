package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.model.User;
import org.hibernate.validator.internal.engine.resolver.JPATraversableResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.social.facebook.api.Account;
import org.springframework.stereotype.Repository;

/**
 * Created by Damien on 11/15/2015.
 */
@RepositoryDefinition(domainClass = User.class, idClass = Long.class)
public interface UserRepository extends JpaRepository<User, Long> {

}
