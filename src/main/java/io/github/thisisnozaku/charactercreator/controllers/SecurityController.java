package io.github.thisisnozaku.charactercreator.controllers;

import io.github.thisisnozaku.charactercreator.authentication.ActivationToken;
import io.github.thisisnozaku.charactercreator.authentication.AppAuthority;
import io.github.thisisnozaku.charactercreator.data.CharacterActivationTokenRepository;
import io.github.thisisnozaku.charactercreator.data.UserRepository;
import io.github.thisisnozaku.charactercreator.authentication.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Damien on 1/31/2016.
 */
@Controller
public class SecurityController {
    @Inject
    private UserRepository users;
    @Inject
    private PasswordEncoder passwordEncoder;
    @Inject
    private CharacterActivationTokenRepository activationTokenRepository;
    @Inject

    private JavaMailSender mailSender;
    @Inject
    private TemplateEngine templateEngine;

    @Value("${users.activation.url}")
    private String activationUrl;

    @RequestMapping(value = "/createuser", method = RequestMethod.POST)
    public String register(User user, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
        try {
            Collection<AppAuthority> authorities = new HashSet<>();
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            authorities.add(new AppAuthority(null, "USER"));
            user.setAuthorities(authorities);
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();
            String authenticationToken = encoder.encodePassword(user.getUsername(), null);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom("neone-character-builder-activation");
            helper.setTo(user.getUsername());
            message.setSubject("Activate Your Account");
            WebContext context = new WebContext(request, response, servletContext);
            context.setVariable("activation", URLEncoder.encode(authenticationToken, "UTF-8"));
            context.setVariable("context_path", activationUrl);
            String body = templateEngine.process("activationmail", context);
            message.setContent(body, "text/html");
            mailSender.send(helper.getMimeMessage());
            user = users.save(user);
            ActivationToken token = new ActivationToken(user.getId(), authenticationToken);
            activationTokenRepository.save(token);
        } catch (MailException | MessagingException | UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
        return "registered";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(User user, Model model){
        model.addAttribute("user", user);
        return "login";
    }

    @RequestMapping(value = "/activate/{token}")
    public String activate(@PathVariable String token){
        ActivationToken activationToken = activationTokenRepository.findOne(token);
        if(activationToken != null){
            User user = users.findOne(activationToken.getid());
            user.setEnabled(true);
            users.saveAndFlush(user);
        }
        return "redirect:/";
    }
}
