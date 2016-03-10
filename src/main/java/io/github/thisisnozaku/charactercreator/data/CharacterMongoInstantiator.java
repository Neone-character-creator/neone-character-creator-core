package io.github.thisisnozaku.charactercreator.data;

import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.data.convert.EntityInstantiator;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.ParameterValueProvider;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by Damien on 2/15/2016.
 */
@Component
public class CharacterMongoInstantiator implements EntityInstantiator {
    @Inject
    private PluginManager pluginManager;

    @Override
    public <T, E extends PersistentEntity<? extends T, P>, P extends PersistentProperty<P>> T createInstance(E entity, ParameterValueProvider<P> provider) {
        Character character = null;
        PluginDescription pluginDescription = null;
        User user = null;
        for(PreferredConstructor.Parameter<?,?> parameter : entity.getPersistenceConstructor().getParameters()){
        }
        return null;
    }
}
