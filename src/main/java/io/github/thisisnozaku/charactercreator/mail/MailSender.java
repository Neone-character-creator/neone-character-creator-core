package io.github.thisisnozaku.charactercreator.mail;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by Damien on 9/15/2016.
 */
@Service
public class MailSender implements EmailSender {
    private AmazonSimpleEmailServiceAsyncClient client;
    private Properties sessionProperties;
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

    @PostConstruct
    public void init() {
        AWSCredentials credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        ClientConfiguration config = new ClientConfiguration();
        this.client = new AmazonSimpleEmailServiceAsyncClient();
        client.setRegion(Region.getRegion(Regions.US_WEST_2));

        sessionProperties = new Properties();
        sessionProperties.setProperty("mail.smtp.auth", auth);
        sessionProperties.setProperty("mail.smtp.starttls.enable", enableTls);
        sessionProperties.setProperty("mail.smtp.starttls.required", requiredTls);
    }

    public void sendMail(MimeMessage incomingMessage) {
        try {
            mailSender.send(incomingMessage);
        } catch (MailException e){
            throw new RuntimeException(e);
        }
    }
}
