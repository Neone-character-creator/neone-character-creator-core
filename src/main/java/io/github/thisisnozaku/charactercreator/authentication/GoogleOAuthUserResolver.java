package io.github.thisisnozaku.charactercreator.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Damie on 2/28/2017.
 */
@Service
public class GoogleOAuthUserResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Person.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String messageBody = webRequest.getNativeRequest(HttpServletRequest.class).getReader().readLine();
        GoogleCredential credentials = new GoogleCredential().setAccessToken(messageBody);

        //TODO: How to inject this?
        Plus.Builder builder = new Plus.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credentials);
        builder.setApplicationName("NEOne Character Builder");

        Plus googlePlus = builder.build();
        Plus.People.Get get = googlePlus.people().get("me");
        Person me = get.execute();
        return me;
    }
}
