package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.RepositoryDefinition;

/**
 * Created by Damien on 11/15/2015.
 */
public interface UserRepository extends JpaRepository<Account, Long> {

}
