package io.github.thisisnozaku.charactercreator.data;

import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Damien on 11/15/2015.
 */
@Repository
@Profile("sqlUserRepository")
public interface UserSqlRepository extends UserRepository {

}
