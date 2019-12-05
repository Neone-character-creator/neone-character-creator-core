package io.github.thisisnozaku.charactercreator.data;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Damien on 11/15/2015.
 */
@Repository
@Profile("mongoUserRepository")
public interface UserMongoRepository extends UserRepository, MongoRepository<OAuthAccountAssociation, Integer> {

}
