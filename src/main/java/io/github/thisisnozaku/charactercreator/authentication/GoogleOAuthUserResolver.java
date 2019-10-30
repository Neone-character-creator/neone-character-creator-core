package io.github.thisisnozaku.charactercreator.authentication;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

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

        //TODO: How to inject this?
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(messageBody);
        PeopleService.People people = new PeopleService.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("NEOne Character Builder").build().people();

        Person me = people.get("people/me").setPersonFields("emailAddresses").execute();
        return me;
    }
}
