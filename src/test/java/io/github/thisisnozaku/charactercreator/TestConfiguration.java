package io.github.thisisnozaku.charactercreator;


import io.github.thisisnozaku.charactercreator.config.ThymeleafConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@ComponentScan(excludeFilters = @ComponentScan.Filter(value = ThymeleafConfig.class, type = FilterType.ASSIGNABLE_TYPE))
public class TestConfiguration {

}