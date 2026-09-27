package com.bridgelabz.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class BridgeLabzOAuth2UserService extends DefaultOAuth2UserService {
    private final String allowedDomain;

    public BridgeLabzOAuth2UserService(
            @Value("${app.security.allowed-email-domain:bridgelabz.com}") String allowedDomain) {
        this.allowedDomain = normalizeDomain(allowedDomain);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User user = super.loadUser(userRequest);
        String email = user.getAttribute("email");
        Object emailVerified = user.getAttribute("email_verified");

        if (email == null || !isAllowedEmail(email) || !isVerified(emailVerified)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("bridgeLabz_email_not_allowed"),
                    "Only verified " + allowedDomain + " Google accounts can access this application.");
        }

        return user;
    }

    private boolean isVerified(Object emailVerified) {
        return emailVerified == null
                || Boolean.TRUE.equals(emailVerified)
                || "true".equalsIgnoreCase(String.valueOf(emailVerified));
    }

    private boolean isAllowedEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String suffix = "@" + allowedDomain;
        return normalizedEmail.endsWith(suffix)
                && normalizedEmail.indexOf('@') == normalizedEmail.length() - suffix.length();
    }

    private static String normalizeDomain(String domain) {
        String normalized = domain == null ? "" : domain.trim().toLowerCase(Locale.ROOT);
        while (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank() || normalized.contains("@") || normalized.contains(" ")) {
            throw new IllegalArgumentException("app.security.allowed-email-domain must be a valid email domain.");
        }
        return normalized;
    }
}
