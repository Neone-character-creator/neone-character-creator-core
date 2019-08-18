package io.github.thisisnozaku.charactercreator.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.internet.MimeMessage;

/**
 * Created by Damien on 9/15/2016.
 */
@Service
@Profile("mail")
public class AppMailSender implements EmailSender {
    @Inject
    private JavaMailSender mailSender;

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private String port;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String auth;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String enableTls;
    @Value("${spring.mail.properties.mail.smtp.starttls.required}")
    private String requiredTls;

    public AppMailSender() {
        super();
    }

    public void sendMail(MimeMessage incomingMessage) {
        try {
            mailSender.send(incomingMessage);
        } catch (MailException e){
            throw new RuntimeException(e);
        }
    }
}
