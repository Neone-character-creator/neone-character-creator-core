package io.github.thisisnozaku.charactercreator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Created by Damien on 1/30/2016.
 */
@Configuration
@EnableWebSecurity
public class
WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Inject
    UserDetailsService userDetailsService;
    @Inject
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(HttpSecurity security) throws Exception {
        security.csrf().csrfTokenRepository(csrfTokenRepository());

        security.authorizeRequests().antMatchers("/", "/login", "/createuser", "/games/" ,"/games/**/pages/info", "/activate/**").permitAll()
                .antMatchers("/js/**", "/css/**").permitAll()
                .antMatchers("/**").authenticated()
                .and()
                .formLogin().loginPage("/login").failureUrl("/login?error").defaultSuccessUrl("/", false)
                .and()
                .logout().permitAll();

        security.userDetailsService(userDetailsService);

        security.headers().frameOptions().sameOrigin();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and().inMemoryAuthentication().withUser("Damien").password("Paranoia").roles("User");
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setSessionAttributeName("_csrf");
        return repository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
