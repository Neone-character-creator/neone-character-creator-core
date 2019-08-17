package io.github.thisisnozaku.charactercreator;


import io.github.thisisnozaku.charactercreator.config.ThymeleafConfig;
import io.github.thisisnozaku.charactercreator.data.access.FileAccessor;
import io.github.thisisnozaku.charactercreator.data.access.LocalFileSystemAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@ComponentScan(excludeFilters = @ComponentScan.Filter(value = ThymeleafConfig.class, type = FilterType.ASSIGNABLE_TYPE))
public class TestConfiguration extends WebMvcConfigurerAdapter {
    @Bean
    static public FileAccessor fileAccess(){
        return new LocalFileSystemAccess();
    }

    @Bean
    static public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(){
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }
}