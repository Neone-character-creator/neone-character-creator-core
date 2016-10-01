package io.github.thisisnozaku.charactercreator.mail;

import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * Created by Damien on 9/15/2016.
 */
@Service
public interface EmailSender {
    void sendMail(MimeMessage message);
}
