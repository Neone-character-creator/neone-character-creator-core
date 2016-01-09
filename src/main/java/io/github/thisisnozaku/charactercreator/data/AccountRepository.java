package io.github.thisisnozaku.charactercreator.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.social.facebook.api.Account;
import org.springframework.stereotype.Repository;

/**
 * Created by Damien on 11/15/2015.
 */
@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

}
