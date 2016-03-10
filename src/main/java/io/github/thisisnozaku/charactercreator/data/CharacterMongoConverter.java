package io.github.thisisnozaku.charactercreator.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.github.thisisnozaku.charactercreator.authentication.User;
import io.github.thisisnozaku.charactercreator.plugins.PluginDescription;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import io.github.thisisnozaku.charactercreator.plugins.Character;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

/**
 * Created by Damien on 1/29/2016.
 */
public class CharacterMongoConverter extends MappingMongoConverter {
    private PluginManager pluginManager;

    public CharacterMongoConverter(DbRefResolver dbRefResolver, MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext, PluginManager pluginManager) {
        super(dbRefResolver, mappingContext);
        this.pluginManager = pluginManager;
    }

    @Override
    public <T> T read(Class<T> clazz, DBObject dbo) {
        if (CharacterDataWrapper.class.isAssignableFrom(clazz)) {
            PluginDescription pluginDescription = read(PluginDescription.class, (DBObject) dbo.get("plugin"));
            Character character = (Character) read(pluginManager.getPlugin(pluginDescription).get().getCharacterType(), (DBObject) dbo.get("character"));
            User user = read(User.class, (DBObject) dbo.get("user"));
            return (T) new CharacterDataWrapper(pluginDescription, user, character);
        }
        return super.read(clazz, dbo);
    }

    @Override
    protected <S> S read(TypeInformation<S> type, DBObject dbo) {
        if (type.getType().isAssignableFrom(Character.class)) {
            return (S) super.read(ClassTypeInformation.from(getTypeForObject(dbo)), dbo);
        } else {
            return super.read(type, dbo);
        }
    }

    private Class<?> getTypeForObject(DBObject dbo) {
        BasicDBObject object = (BasicDBObject) dbo.get("pluginDescription");
        PluginDescription pluginDescription = new PluginDescription(object.getString("author"), object.getString("system"), object.getString("version"));
        return pluginManager.getPlugin(pluginDescription).get().getCharacterType();
    }
}
