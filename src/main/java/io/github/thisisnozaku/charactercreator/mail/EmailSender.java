package io.github.thisisnozaku.charactercreator.mail;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

/**
 * Created by Damien on 9/15/2016.
 */
public interface EmailSender {
    void sendMail(MimeMessage message);
}
