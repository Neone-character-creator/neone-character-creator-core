package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.plugins.Character;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

/**
 * Created by Damien on 1/18/2016.
 */
public interface CharacterMongoRepository extends MongoRepository<Character, BigInteger>{
}