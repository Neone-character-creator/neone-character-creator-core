package io.github.thisisnozaku.charactercreator.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
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
import org.springframework.stereotype.Component;

import javax.inject.Inject;

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
        if (Character.class.isAssignableFrom(clazz)) {
            return (T) super.read(getTypeForObject(dbo), dbo);
        } else {
            return super.read(clazz, dbo);
        }
    }

    @Override
    protected <S> S read(TypeInformation<S> type, DBObject dbo) {
        if (type.getType().isAssignableFrom(Character.class)) {
            return (S) super.read(ClassTypeInformation.from(getTypeForObject(dbo)), dbo);
        } else {
            return super.read(type, dbo);
        }
    }

    private Class<?> getTypeForObject(DBObject dbo){
        BasicDBObject object = (BasicDBObject) dbo.get("pluginDescription");
        PluginDescription pluginDescription = new PluginDescription(object.getString("author"), object.getString("system"), object.getString("version"));
        return pluginManager.getPlugin(pluginDescription).get().getCharacterType();
    }
}
