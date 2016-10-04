package io.github.thisisnozaku.charactercreator;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableAspectJAutoProxy
public class NeoneCoreApplication {

    public static void main(String[] args) {
        run(NeoneCoreApplication.class, args);
    }
}