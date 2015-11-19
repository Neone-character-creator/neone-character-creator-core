package io.github.thisisnozaku.charactercreator;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(exclude = {HibernateJpaAutoConfiguration.class})
public class NeoneCoreApplication {

    public static void main(String[] args) {
        run(NeoneCoreApplication.class, args);
    }
}