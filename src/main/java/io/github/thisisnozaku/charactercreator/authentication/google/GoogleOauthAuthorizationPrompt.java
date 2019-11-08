package io.github.thisisnozaku.charactercreator.authentication.google;

/**
 * Prompt sent to the user to prompt them to authorize the application.
 */
public class GoogleOauthAuthorizationPrompt {
    public final String redirectUrl;

    public GoogleOauthAuthorizationPrompt(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
