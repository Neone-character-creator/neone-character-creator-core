package io.github.thisisnozaku.charactercreator.config;

import io.github.thisisnozaku.charactercreator.NeoneCoreApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * Created by Damien on 9/11/2016.
 */
public class ServletConfig extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(NeoneCoreApplication.class);
    }
}
