package com.bridgelabz.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PccoeAccessService {
    private final Set<String> allowedEmails;

    public PccoeAccessService(
            @Value("${app.security.pccoe-allowed-emails:}") String configuredEmails) {
        allowedEmails = Arrays.stream(configuredEmails.split(","))
                .map(String::trim)
                .filter(email -> !email.isBlank())
                .map(email -> email.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean canAccess(String email) {
        return email != null && allowedEmails.contains(email.toLowerCase(Locale.ROOT));
    }
}
