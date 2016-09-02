package io.github.thisisnozaku.charactercreator.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Created by Damien on 1/18/2016.
 */
@NoRepositoryBean
public interface CharacterMongoRepository extends MongoRepository<CharacterDataWrapper, String>{
}