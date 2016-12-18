package io.github.thisisnozaku.charactercreator;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableAspectJAutoProxy
@EntityScan
public class NeoneCoreApplication {
    public static void main(String[] args) {
        run(NeoneCoreApplication.class, args);
    }
}