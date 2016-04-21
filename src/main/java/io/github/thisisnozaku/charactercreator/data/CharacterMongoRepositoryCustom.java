package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import org.springframework.data.mongodb.repository.Query;

import java.security.Principal;
import java.util.List;

/**
 * Created by Damien on 4/17/2016.
 */
public interface CharacterMongoRepositoryCustom extends CharacterMongoRepository {
    @Query()
    List<CharacterDataWrapper> findByUserAndPlugin(User user, PluginDescription plugin);
}
