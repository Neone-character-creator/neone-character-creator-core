package io.github.thisisnozaku.charactercreator;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class NeoneCoreApplication {

    public static void main(String[] args) {
        run(NeoneCoreApplication.class, args);
    }
}