package com.bridgelabz.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PccoeAccessServiceTest {

    @Test
    void allowsConfiguredEmailsCaseInsensitively() {
        PccoeAccessService service =
                new PccoeAccessService("lead@bridgelabz.com, ADMIN@bridgelabz.com");

        assertTrue(service.canAccess("LEAD@BRIDGELABZ.COM"));
        assertTrue(service.canAccess("admin@bridgelabz.com"));
    }

    @Test
    void rejectsUnconfiguredAndMissingEmails() {
        PccoeAccessService service =
                new PccoeAccessService("lead@bridgelabz.com");

        assertFalse(service.canAccess("staff@bridgelabz.com"));
        assertFalse(service.canAccess(null));
        assertFalse(service.canAccess(""));
    }

    @Test
    void emptyConfigurationDeniesEveryone() {
        PccoeAccessService service = new PccoeAccessService("");

        assertFalse(service.canAccess("lead@bridgelabz.com"));
    }
}
