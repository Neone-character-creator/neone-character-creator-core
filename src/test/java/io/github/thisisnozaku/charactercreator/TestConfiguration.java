package io.github.thisisnozaku.charactercreator;


import io.github.thisisnozaku.charactercreator.config.ThymeleafConfig;
import io.github.thisisnozaku.charactercreator.data.access.FileAccessor;
import io.github.thisisnozaku.charactercreator.data.access.LocalFileSystemAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@ComponentScan(excludeFilters = @ComponentScan.Filter(value = ThymeleafConfig.class, type = FilterType.ASSIGNABLE_TYPE))
public class TestConfiguration {
    @Bean
    public FileAccessor fileAccess(){
        return new LocalFileSystemAccess();
    }
}