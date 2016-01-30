package io.github.thisisnozaku.charactercreator.config;

import com.mongodb.Mongo;
import io.github.thisisnozaku.charactercreator.data.CharacterMongoConverter;
import io.github.thisisnozaku.charactercreator.plugins.PluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.CustomConversions;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import javax.inject.Inject;
import java.net.UnknownHostException;

/**
 * Created by Damien on 11/15/2015.
 */
@Configuration
public class MongoConfig {
    @Inject
    private PluginManager pluginManager;

    @Bean
    public MappingMongoConverter mongoConverter() throws UnknownHostException {
        MongoMappingContext context = new MongoMappingContext();
        DbRefResolver refResolver = new DefaultDbRefResolver(new SimpleMongoDbFactory(new Mongo(), "character"));
        return new CharacterMongoConverter(refResolver, context, pluginManager);
    }
}
